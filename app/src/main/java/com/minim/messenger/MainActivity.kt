package com.minim.messenger

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.main_activity.*


class MainActivity : AppCompatActivity() {

    private lateinit var linearLayoutManager: LinearLayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        linearLayoutManager = LinearLayoutManager(this)
        contactsRecyclerView.layoutManager = linearLayoutManager
        initRecyclerView()

    }

    private fun initRecyclerView() {
        val user = User("MICHA","1234567","012711")
        val adapter = ContactsAdaptor(arrayListOf(user,user,user))
        contactsRecyclerView.adapter = adapter
        contactsRecyclerView.layoutManager = LinearLayoutManager(this)
    }
}