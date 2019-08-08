package com.minim.messenger.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentChange.Type.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.minim.messenger.R
import com.minim.messenger.adapters.ConversationsAdapter
import com.minim.messenger.adapters.MessagesAdapter
import com.minim.messenger.models.Conversation
import com.minim.messenger.models.Message
import com.minim.messenger.util.Navigation
import kotlinx.android.synthetic.main.activity_conversation_log.*
import java.util.concurrent.TimeUnit

class ConversationLogActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private var initialLoad: Boolean = true
    private lateinit var conversation: Conversation
    private lateinit var adapter: MessagesAdapter
    private lateinit var messagesListener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_log)

        conversation = intent.getParcelableExtra<Conversation>("conversation")!!
        adapter = MessagesAdapter(conversation.messages)

        initRecyclerView()
        initConversationListener()
        contact_username_text_view.text = conversation.other.username
        initDurationSettings()

        attach_button.setOnClickListener { sendEmail() }

        send_button.setOnClickListener {
            if (!pickers_linear_layout.isShown) {
                sendMessage(message_edit_text.text.toString())
            }
        }

        send_button.setOnLongClickListener {
            if (pickers_linear_layout.isShown) {
                input_linear_layout.visibility = View.VISIBLE
                pickers_linear_layout.visibility = View.GONE
                send_button.setImageResource(R.drawable.send_icon)
                message_edit_text.requestFocus()
            } else {
                input_linear_layout.visibility = View.GONE
                pickers_linear_layout.visibility = View.VISIBLE
                send_button.setImageResource(R.drawable.close_icon)
            }
            return@setOnLongClickListener true
        }

    }

    private fun initRecyclerView() {
        messages_recycler_view.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        messages_recycler_view.layoutManager = linearLayoutManager
    }

    private fun initDurationSettings() {

        hours_picker.maxValue = 24
        minutes_picker.maxValue = 59
        seconds_picker.maxValue = 59

        // Defaults
        hours_picker.value = 0
        minutes_picker.value = 0
        seconds_picker.value = 10

    }

    private fun sendEmail() {
        val appName = resources.getString(R.string.app_name)
        val subject = "[$appName] Attachment from ${conversation.user.username}"
        Navigation.sendEmail(this, conversation.other.email!!, subject)
    }

    private fun sendMessage(messageText: String) {

        if (messageText.isEmpty()) return

        val docRef = firestore.collection("messages").document()
        val message = Message(
            id = docRef.id,
            conversationId = conversation.id,
            sender = conversation.user.uid,
            receiver = conversation.other.uid,
            content = messageText,
            duration = getDurationValue()
        )

        conversation.messages.add(message)
        adapter.notifyDataSetChanged()
        messages_recycler_view.scrollToPosition(conversation.messages.lastIndex)
        message_edit_text.text.clear()

        val encryptedMessage = message.copy().also { it.encrypt() }
        docRef.set(encryptedMessage).addOnCompleteListener {
            firestore.collection("conversations").document(conversation.id!!)
                .update("messages", FieldValue.arrayUnion(message.id))
        }

    }

    private fun getDurationValue(): Long {
        val hours = hours_picker.value.toLong()
        val minutes = minutes_picker.value.toLong()
        val seconds = seconds_picker.value.toLong()
        return TimeUnit.HOURS.toSeconds(hours) + TimeUnit.MINUTES.toSeconds(minutes) + seconds
    }

    private fun initConversationListener() {

        messagesListener = firestore.collection("messages")
            .whereEqualTo("conversationId", conversation.id)
            .addSnapshotListener { snapshots, e ->
                if (e != null || initialLoad) {
                    initialLoad = false
                    return@addSnapshotListener
                }
                for (dc in snapshots!!.documentChanges) {
                    val message = dc.document.toObject(Message::class.java)
                    when (dc.type) {
                        ADDED -> addNewMessage(message)
                        MODIFIED -> updateMessageSeen(message)
                        REMOVED -> removeMessage(message)
                    }
                }
            }

    }

    private fun addNewMessage(message: Message) {

        if (!message.isFromOther(conversation.other.uid!!)) return
        message.markAsSeen()
        firestore.collection("messages").document(message.id!!).set(message)

        message.determineType(conversation.other.uid!!)
        message.decrypt()

        conversation.messages.add(message)
        adapter.notifyDataSetChanged()
        messages_recycler_view.scrollToPosition(conversation.messages.lastIndex)

    }

    private fun updateMessageSeen(newMessage: Message) {
        newMessage.determineType(conversation.other.uid!!)
        val index = conversation.getMessageIndex(newMessage.id!!)
        val message = conversation.messages[index]
        message.seen = newMessage.seen
        message.seenOn = newMessage.seenOn
        message.deleteOn = newMessage.deleteOn
        adapter.notifyItemChanged(index)
    }

    private fun removeMessage(message: Message) {
        val index = conversation.getMessageIndex(message.id!!)
        conversation.messages.removeAt(index)
        adapter.notifyItemRemoved(index)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        messagesListener.remove()
        ConversationsAdapter.currentConversationId = ""
    }
}

