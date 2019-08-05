package com.minim.messenger.activities

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange.Type.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.minim.messenger.R
import com.minim.messenger.adapters.ConversationsAdapter
import com.minim.messenger.models.Conversation
import com.minim.messenger.models.Message
import com.minim.messenger.models.User
import kotlinx.android.synthetic.main.activity_conversations.*

class ConversationsActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var currentUser: User
    private lateinit var adapter: ConversationsAdapter
    private var conversations = arrayListOf<Conversation>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)

        currentUser = User(auth.currentUser!!)
        initRecyclerView()
        initConversationListeners()

        add_contact_button.setOnClickListener {
            if (SettingsActivity().isUsernameValid(this, search_edit_text)) {
                addContact(search_edit_text.text.toString())
            }
        }

        settings_button.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        search_edit_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) = Unit
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s.toString())
            }
        })

    }

    private fun initRecyclerView() {
        adapter = ConversationsAdapter(this, conversations)
        contacts_recycler_view.adapter = adapter
        contacts_recycler_view.layoutManager = LinearLayoutManager(this)
    }


    private fun contactExists(username: String): Boolean {
        conversations.find { it.other.username.equals(username) } ?: return false
        return true
    }

    private fun addContact(username: String) {

        if (contactExists(username) || username == currentUser.username) {
            search_edit_text.error = "Contact already exists"
            return
        }

        firestore.collection("users")
            .whereEqualTo("username", username)
            .get().addOnCompleteListener {

                if (it.result!!.isEmpty) {
                    search_edit_text.error = "User not found"
                    return@addOnCompleteListener
                }

                search_edit_text.text.clear()
                search_edit_text.clearFocus()

                val docRef = firestore.collection("conversations").document()
                val contact = it.result!!.documents.first().toObject(User::class.java)!!
                val conversation = Conversation(docRef.id, arrayListOf(currentUser, contact))
                docRef.set(conversation.document)

            }

    }

    private fun initConversationListeners() {
        firestore.collection("conversations")
            .whereArrayContains("participants", currentUser.uid!!)
            .addSnapshotListener { querySnapshot, _ ->

                if (querySnapshot!!.isEmpty) {
                    contacts_progress_bar.visibility = View.GONE
                }

                val changes = querySnapshot.documentChanges
                for ((i, dc) in changes.withIndex()) {
                    when (dc.type) {
                        ADDED -> fetchContacts(dc.document, i == changes.lastIndex)
                        MODIFIED -> newMessageNotification(dc.document)
                        REMOVED -> {
                        }
                    }
                }

            }
    }

    @Suppress("UNCHECKED_CAST")
    private fun fetchContacts(document: DocumentSnapshot, isLast: Boolean) {

        val id = document.data!!["id"].toString()
        val participants = document.data!!["participants"] as ArrayList<String>
        val otherUid = participants.find { it != currentUser.uid }!!

        firestore.collection("users")
            .document(otherUid)
            .get()
            .addOnSuccessListener {
                val contact = it.toObject(User::class.java)!!
                val conversation = Conversation(id, arrayListOf(currentUser, contact))
                adapter.conversations.add(conversation)
            }.addOnCompleteListener {
                if (isLast) {
                    contacts_progress_bar.visibility = View.GONE
                    adapter.notifyDataSetChanged()
                }
            }
    }

    @Suppress("UNCHECKED_CAST")
    private fun newMessageNotification(document: DocumentSnapshot) {

        val messagesIds = document.data!!["messages"] as ArrayList<String>
        if (messagesIds.isEmpty()) {
            return
        }

        val docRef = firestore.collection("messages").document(messagesIds.last())
        docRef.get().addOnSuccessListener {
            if (!it.exists()) {
                return@addOnSuccessListener
            }
            val message = it.toObject(Message::class.java)!!
            if (message.sender == currentUser.uid) {
                return@addOnSuccessListener
            }
            val id = document.data!!["id"].toString()
            val index = adapter.conversations.indexOfFirst { c -> c.id == id }
            adapter.conversations[index].hasChanges = true
            adapter.notifyItemChanged(index)
            val other = adapter.conversations[index].participants.find { u -> u.uid == message.sender }!!
            pushNotification(message, other.username!!, index)
        }

    }

    private fun pushNotification(message: Message, sender: String, index: Int) {
        val intent = Intent(this, ConversationsActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(sender)
            .setContentText(message.content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(alarmSound)
            .build()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(index, builder)
    }

    override fun onResume() {
        super.onResume()
        if (currentConversationIndex == -1) return
        conversations[currentConversationIndex].hasChanges = false
        adapter.notifyItemChanged(currentConversationIndex)
    }

    override fun finish() {
        super.finish()
        currentConversationIndex = -1
    }

    companion object {
        var currentConversationIndex = -1
    }

}
