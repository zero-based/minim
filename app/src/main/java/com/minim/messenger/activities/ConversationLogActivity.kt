package com.minim.messenger.activities

import android.content.Intent
import android.os.Bundle
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
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import android.net.Uri


class ConversationLogActivity : AppCompatActivity() {

    private lateinit var conversation: Conversation
    private lateinit var adapter: MessagesAdapter
    private lateinit var registrationListener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_log)

        conversation = intent.getParcelableExtra<Conversation>("conversation")!!
        adapter = MessagesAdapter(conversation.messages)

        contact_username_text_view.text = conversation.participants[1].username
        initRecyclerView()
        initConversationListener(conversation.id)

        attach_button.setOnClickListener {
            val sender = conversation.participants[0]
            val receiver = conversation.participants[1]
            val emailIntent = Intent(
                Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "${receiver.email}", null
                )
            )
            val subject = "[${R.string.app_name}] Attachment from ${sender.username}"
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            startActivity(Intent.createChooser(emailIntent, "Send email..."))
        }

        send_button.setOnClickListener {

            if (message_edit_text.text.toString().isEmpty()) {
                return@setOnClickListener
            }

            val message = Message(
                conversation.participants[0].username,
                conversation.participants[1].username,
                Message.Type.TO,
                message_edit_text.text.toString(),
                false,
                1440,
                Timestamp.now(),
                Timestamp.now()
            )

            adapter.messages.add(message)
            messages_recycler_view.scrollToPosition(adapter.itemCount - 1)
            adapter.notifyDataSetChanged()
            message_edit_text.text.clear()

            FirebaseFirestore.getInstance()
                .collection("conversations")
                .document(conversation.id)
                .update("messages", FieldValue.arrayUnion(message)).addOnFailureListener {
                    // TODO: Message not sent warning!
                }
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
    private fun fetchNewMessage(messages: Any?) {
        messages as ArrayList<HashMap<String, *>>
        val message = Message(messages[messages.lastIndex])
        if (message.sender == conversation.participants[0].username) {
            return
        }
        message.type = Message.Type.FROM
        conversation.messages.add(message)
        messages_recycler_view.scrollToPosition(adapter.itemCount - 1)
        adapter.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        registrationListener.remove()
    }
}