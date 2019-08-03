package com.minim.messenger.activities

import android.content.Intent
import android.net.Uri
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

class ConversationLogActivity : AppCompatActivity() {

    private lateinit var conversation: Conversation
    private lateinit var adapter: MessagesAdapter
    private lateinit var registrationListener: ListenerRegistration
    private val firestore = FirebaseFirestore.getInstance()
    private var isFirstTime = true

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_log)

        conversation = intent.getParcelableExtra<Conversation>("conversation")!!
        adapter = MessagesAdapter(conversation.messages)

        contact_username_text_view.text = conversation.other.username
        initRecyclerView()
        initConversationListener()

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
                id = docRef.id,
                conversationId = conversation.id,
                sender = conversation.user.uid,
                receiver = conversation.other.uid,
                type = Message.Type.TO,
                content = message_edit_text.text.toString(),
                seen = false,
                duration = 10,
                sentOn = Timestamp.now(),
                seenOn = null,
                deleteOn = null
            )

            docRef.set(message).addOnCompleteListener {
                firestore.collection("conversations").document(conversation.id!!)
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

    private fun initConversationListener() {

        registrationListener = firestore.collection("messages")
            .whereEqualTo("conversationId", conversation.id)
            .addSnapshotListener { snapshots, e ->
                if (e != null || isFirstTime) {
                    isFirstTime = false
                    return@addSnapshotListener
                }
                for (dc in snapshots!!.documentChanges) {

                    val message = dc.document.toObject(Message::class.java)
                    val i = conversation.getMessageIndex(message.id!!)

                    when {
                        dc.type == DocumentChange.Type.ADDED -> fetchNewMessage(message)
                        dc.type == DocumentChange.Type.MODIFIED -> updateMessageData(i, message)
                        dc.type == DocumentChange.Type.REMOVED -> removeMessage(i)
                    }
                }
            }
    }

    @Suppress("UNCHECKED_CAST")
    private fun fetchNewMessage(message: Message) {
        val isSenderCurrentUser = message.sender == conversation.user.uid
        if (isSenderCurrentUser) {
            return
        }
        message.markAsSeen()
        firestore.collection("messages").document(message.id!!).set(message)
        message.type = Message.Type.FROM
        conversation.messages.add(message)
        messages_recycler_view.scrollToPosition(adapter.itemCount - 1)
        adapter.notifyDataSetChanged()
    }

    private fun updateMessageData(index: Int, newMessage: Message) {
        if (newMessage.sender == conversation.other.uid) {
            newMessage.type = Message.Type.FROM
        }
        conversation.messages[index] = newMessage
        adapter.notifyItemChanged(index)
    }

    private fun removeMessage(index: Int) {
        conversation.messages.removeAt(index)
        adapter.notifyItemRemoved(index)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        registrationListener.remove()
    }

}