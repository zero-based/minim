package com.minim.messenger.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.minim.messenger.R
import com.minim.messenger.adapters.ConversationAdapter
import com.minim.messenger.models.Conversation
import kotlinx.android.synthetic.main.activity_conversation.*

class ConversationActivity : AppCompatActivity() {

    private lateinit var conversation: Conversation
    private lateinit var adapter: ConversationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        conversation = intent.getParcelableExtra<Conversation>("conversation")!!
        adapter = ConversationAdapter(conversation.messages!!)

        contact_username_text_view.text = conversation.participant_2!!.username
        initRecyclerView()

        send_button.setOnClickListener {
            /* TODO : Construct message object from message_edit_text, then add:
             *    1) The message to conversation local variable & notify the adaptor
             *    2) The message document to Firestore messages collections
             *    3) Message.id to conversation Firestore document
             */
        }
    }

    private fun initRecyclerView() {
        messages_recycler_view.adapter = adapter
        messages_recycler_view.layoutManager = LinearLayoutManager(this)
    }

}