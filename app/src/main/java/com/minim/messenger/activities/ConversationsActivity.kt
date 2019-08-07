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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.minim.messenger.R
import com.minim.messenger.adapters.ConversationsAdapter
import com.minim.messenger.models.Conversation
import com.minim.messenger.models.Message
import com.minim.messenger.models.User
import com.minim.messenger.util.Security
import com.minim.messenger.util.SharedPrefHelper
import com.minim.messenger.util.Validation
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
            if (Validation.isUsernameValid(this, search_edit_text)) {
                addContact(search_edit_text.text.toString())
            } else {
                add_contact_button.visibility = View.GONE
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
                val query = s.toString()
                adapter.filter.filter(query) {
                    if (query == currentUser.username || adapter.itemCount != 0 || query.isEmpty()) {
                        add_contact_button.visibility = View.GONE
                    } else {
                        add_contact_button.visibility = View.VISIBLE
                    }
                }
            }
        })

    }

    private fun initRecyclerView() {
        adapter = ConversationsAdapter(this, conversations)
        contacts_recycler_view.adapter = adapter
        contacts_recycler_view.layoutManager = LinearLayoutManager(this)
    }

    private fun addContact(username: String) {

        firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener {

                if (it.isEmpty) {
                    add_contact_button.visibility = View.GONE
                    search_edit_text.error = "User not found"
                    return@addOnSuccessListener
                }

                search_edit_text.text.clear()
                search_edit_text.clearFocus()

                val docRef = firestore.collection("conversations").document()
                val contact = it.documents.first().toObject(User::class.java)!!
                val secret = Security.getRandomString()
                val conversation = Conversation(docRef.id, secret, arrayListOf(currentUser, contact))
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
                val newSize = conversations.size + changes.count { it.type == ADDED }

                for (dc in changes) {
                    when (dc.type) {
                        ADDED -> addConversation(dc.document, newSize)
                        MODIFIED -> newMessageNotification(dc.document)
                        REMOVED -> {
                        }
                    }
                }

            }
    }

    @Suppress("UNCHECKED_CAST")
    private fun addConversation(document: QueryDocumentSnapshot, newSize: Int) {

        val conversation = Conversation(document.data, currentUser)
        val creatorUid = (document.data["participants"] as ArrayList<String>).first()

        val id = conversation.id!!
        if (conversation.secret.isNullOrEmpty()) {
            conversation.secret = SharedPrefHelper.getString(this, id)
        } else {
            SharedPrefHelper.addString(this, id, conversation.secret!!)
            if (creatorUid != currentUser.uid) {
                // To make sure that the user who created the
                // conversation doesn't delete the secret
                // before the other save it on his phone.
                firestore.collection("conversations").document(id)
                    .update(hashMapOf<String, Any>("secret" to FieldValue.delete()))
            }
        }

        firestore.collection("users")
            .document(conversation.other.uid!!)
            .get()
            .addOnSuccessListener {
                conversation.initOther(it.toObject(User::class.java)!!)
                conversations.add(conversation)
                if (conversations.size == newSize) { // Last addition
                    contacts_progress_bar.visibility = View.GONE
                    conversations.sortBy { c -> c.other.username }
                    adapter.notifyDataSetChanged()
                }
            }

    }

    @Suppress("UNCHECKED_CAST")
    private fun newMessageNotification(document: QueryDocumentSnapshot) {

        val messagesIds = document.data["messages"] as ArrayList<String>
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
            val id = document.data["id"].toString()
            val index = conversations.indexOfFirst { c -> c.id == id }
            val conversation = conversations[index]
            Security.setKey(conversation.secret!!)
            message.also { m -> m.content = Security.decrypt(m.content!!) }
            pushNotification(message, conversation.other.username!!, index)
            conversation.hasChanges = true
            adapter.notifyItemChanged(index)
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

}
