package com.minim.messenger.adaptors

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.minim.messenger.R
import com.minim.messenger.activities.ConversationActivity
import com.minim.messenger.models.User

class ContactsAdaptor(private val context: Context, val contacts: ArrayList<User>) :
    RecyclerView.Adapter<ContactsAdaptor.ContactHolder>() {

    override fun getItemCount() = contacts.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ContactHolder(LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false))

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        holder.contactUsername.text = contacts[position].username
        holder.parentLayout.setOnClickListener {
            val intent = Intent(context, ConversationActivity::class.java)
            context.startActivity(intent)
        }
    }

    inner class ContactHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var contactUsername: TextView = itemView.findViewById(R.id.contactUsername)
        internal var parentLayout: LinearLayout = itemView.findViewById(R.id.contactLinearLayout)
    }

}