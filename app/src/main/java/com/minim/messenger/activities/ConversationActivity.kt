package com.minim.messenger.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.minim.messenger.R
import com.minim.messenger.adapters.ConversationAdapter
import com.minim.messenger.models.Message
import kotlinx.android.synthetic.main.activity_conversation.*
import com.google.firebase.Timestamp
import com.minim.messenger.models.User

class ConversationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)
        contact_username_text_view.text = intent.getParcelableExtra<User>("contact").username
        initRecyclerView()
    }

    private fun initRecyclerView() {
        val content = "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z !"
        val messageFrom = Message(
            "12324",
            "1324",
            "1324",
            Message.Type.FROM,
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
            Message.Type.TO,
            content,
            true,
            3,
            Timestamp.now(),
            Timestamp.now()
        )
        val adapter = ConversationAdapter(arrayListOf(messageFrom, messageTo, messageFrom, messageFrom))
        messages_recycler_view.adapter = adapter
        messages_recycler_view.layoutManager = LinearLayoutManager(this)
    }

}