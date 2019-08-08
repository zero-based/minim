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
            if (duration_value_picker.isShown) {
                duration_value_picker.visibility = View.GONE
                duration_unit_picker.visibility = View.GONE
                message_edit_text.visibility = View.VISIBLE
                attach_button.visibility = View.VISIBLE
                message_edit_text.requestFocus()
            }
            sendMessage(message_edit_text.text.toString())
        }

        send_button.setOnLongClickListener {
            if (duration_value_picker.isShown) {
                duration_value_picker.visibility = View.GONE
                duration_unit_picker.visibility = View.GONE
                message_edit_text.visibility = View.VISIBLE
                attach_button.visibility = View.VISIBLE
                message_edit_text.requestFocus()
            } else {
                message_edit_text.visibility = View.GONE
                attach_button.visibility = View.GONE
                duration_value_picker.visibility = View.VISIBLE
                duration_unit_picker.visibility = View.VISIBLE
            }
            return@setOnLongClickListener true
        }

        duration_unit_picker.setOnValueChangedListener { _, _, newValueIndex ->
            when (newValueIndex) {
                0 -> duration_value_picker.maxValue = 59
                1 -> duration_value_picker.maxValue = 59
                2 -> duration_value_picker.maxValue = 24
                3 -> duration_unit_picker.value = 0
            }
        }
    }

    private fun initRecyclerView() {
        messages_recycler_view.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        messages_recycler_view.layoutManager = linearLayoutManager
    }

    private fun initDurationSettings() {
        val values = arrayOf("sec.", "min.", "hr.", "")
        duration_unit_picker.minValue = 0
        duration_unit_picker.maxValue = values.lastIndex
        duration_unit_picker.displayedValues = values
        duration_unit_picker.wrapSelectorWheel = true
        duration_value_picker.minValue = 1
        duration_value_picker.maxValue = 24
        duration_value_picker.value = 24
        duration_unit_picker.value = values.indexOf("hr.")
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

    private fun sendEmail() {
        val appName = resources.getString(R.string.app_name)
        val subject = "[$appName] Attachment from ${conversation.user.username}"
        Navigation.sendEmail(this, conversation.other.email!!, subject)
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

    private fun getDurationValue(): Long {
        val value = duration_value_picker.value.toLong()
        return when (duration_unit_picker.value) {
            0 -> value
            1 -> TimeUnit.MINUTES.toSeconds(value)
            2 -> TimeUnit.HOURS.toSeconds(value)
            else -> TimeUnit.HOURS.toSeconds(resources.getInteger(R.integer.default_destruction_hours).toLong())
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        messagesListener.remove()
        ConversationsAdapter.currentConversationId = ""
    }
}

