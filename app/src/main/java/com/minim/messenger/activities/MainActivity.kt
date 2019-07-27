package com.minim.messenger.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.minim.messenger.adaptors.ContactsAdaptor
import com.minim.messenger.R
import com.minim.messenger.models.User
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        val user = User("101", "@username", "+00000000")
        val adapter = ContactsAdaptor(this, arrayListOf(user, user, user))
        contactsRecyclerView.adapter = adapter
        contactsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

}