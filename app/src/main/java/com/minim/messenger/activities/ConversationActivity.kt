package com.minim.messenger.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.minim.messenger.R
import com.minim.messenger.adaptors.ConversationAdaptor
import com.minim.messenger.models.Message
import kotlinx.android.synthetic.main.activity_conversation.*
import com.google.firebase.Timestamp
import com.minim.messenger.models.MessageType

class ConversationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)
        initRecyclerView()

    }

    private fun initRecyclerView() {
        val content = "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z !"
        val messageFrom = Message(
            "12324",
            "1324",
            "1324",
            MessageType.FROM,
            content,
            true,
            3,
            Timestamp.now(),
            Timestamp.now()
        )
        val messageTo = Message(
            "12324",
            "1324",
            "1324",
            MessageType.TO,
            content,
            true,
            3,
            Timestamp.now(),
            Timestamp.now()
        )
        val adapter = ConversationAdaptor(arrayListOf(messageFrom, messageTo, messageFrom, messageFrom))
        messagesRecyclerView.adapter = adapter
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
    }

}