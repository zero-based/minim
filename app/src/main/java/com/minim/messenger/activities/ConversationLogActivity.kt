package com.minim.messenger.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.minim.messenger.R
import com.minim.messenger.adapters.MessagesAdapter
import com.minim.messenger.models.Conversation
import com.minim.messenger.models.Message
import kotlinx.android.synthetic.main.activity_conversation_log.*

class ConversationLogActivity : AppCompatActivity() {

    private lateinit var conversation: Conversation
    private lateinit var adapter: MessagesAdapter
    private lateinit var registrationListener: ListenerRegistration
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_log)

        conversation = intent.getParcelableExtra<Conversation>("conversation")!!
        adapter = MessagesAdapter(conversation.messages)

        contact_username_text_view.text = conversation.other.username
        initRecyclerView()
        initConversationListener(conversation.id)

        attach_button.setOnClickListener {
            val emailIntent = Intent(
                Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "${conversation.other.email}", null
                )
            )
            val appName = resources.getString(R.string.app_name)
            val subject = "[$appName] Attachment from ${conversation.user.username}"
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            startActivity(Intent.createChooser(emailIntent, "Send email..."))
        }

        send_button.setOnClickListener {

            if (message_edit_text.text.toString().isEmpty()) {
                return@setOnClickListener
            }
            val docRef = firestore.collection("messages").document()
            val message = Message(
                docRef.id,
                conversation.id,
                conversation.user.username,
                conversation.other.username,
                Message.Type.TO,
                message_edit_text.text.toString(),
                false,
                1440,
                Timestamp.now(),
                null
            )
            docRef.set(message).addOnCompleteListener {
                firestore.collection("conversations").document(conversation.id)
                    .update("messages", FieldValue.arrayUnion(message.id))
            }

            adapter.messages.add(message)
            messages_recycler_view.scrollToPosition(adapter.itemCount - 1)
            adapter.notifyDataSetChanged()
            message_edit_text.text.clear()
        }
    }

    private fun initRecyclerView() {
        messages_recycler_view.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        messages_recycler_view.layoutManager = linearLayoutManager
    }

    private fun initConversationListener(conversationId: String) {
        registrationListener = FirebaseFirestore.getInstance().collection("conversations")
            .whereEqualTo("id", conversationId)
            .addSnapshotListener { querySnapshot, _ ->
                val docChange = querySnapshot!!.documentChanges.first()
                if (docChange.type == DocumentChange.Type.MODIFIED) {
                    fetchNewMessage(docChange.document.data["messages"])
                }
            }
    }

    @Suppress("UNCHECKED_CAST")
    private fun fetchNewMessage(messagesIds: Any?) {

        messagesIds as ArrayList<String>
        val docRef = firestore.collection("messages").document(messagesIds.last())
        docRef.get().addOnSuccessListener {
            val message = it.toObject(Message::class.java)!!
            if (message.sender == conversation.user.username) {
                return@addOnSuccessListener
            }
            message.markAsSeen()
            docRef.set(message)
            message.type = Message.Type.FROM

            conversation.messages.add(message)
            messages_recycler_view.scrollToPosition(adapter.itemCount - 1)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        registrationListener.remove()
    }

}