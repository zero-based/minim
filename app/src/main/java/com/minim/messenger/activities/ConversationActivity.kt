package com.minim.messenger.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.minim.messenger.R
import com.minim.messenger.adapters.ConversationAdapter
import com.minim.messenger.models.Conversation
import com.minim.messenger.models.Message
import kotlinx.android.synthetic.main.activity_conversation.*

class ConversationActivity : AppCompatActivity() {

    private lateinit var conversation: Conversation
    private lateinit var adapter: ConversationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        conversation = intent.getParcelableExtra<Conversation>("conversation")!!
        adapter = ConversationAdapter(conversation.messages)

        contact_username_text_view.text = conversation.participants[1].username
        initRecyclerView()
        initMessagesListeners(conversation.id)

        send_button.setOnClickListener {

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
        messages_recycler_view.layoutManager = LinearLayoutManager(this)
    }

    private fun initMessagesListeners(conversationId: String) {
        FirebaseFirestore.getInstance().collection("conversations")
            .document(conversationId)
            .addSnapshotListener { snapshot, firestoreException ->
                // TODO: Reload messages
            }
    }

}