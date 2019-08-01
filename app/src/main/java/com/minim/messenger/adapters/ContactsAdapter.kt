package com.minim.messenger.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.minim.messenger.R
import com.minim.messenger.activities.ConversationActivity
import com.minim.messenger.models.User

class ContactsAdapter(private val context: Context, val contacts: ArrayList<User>) :
    RecyclerView.Adapter<ContactsAdapter.ContactHolder>(), Filterable {

    private var filteredContacts = contacts

    override fun getItemCount() = filteredContacts.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ContactHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false))

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        val contact = filteredContacts[position]
        holder.contactUsername.text = contact.username
        holder.parentLayout.setOnClickListener {
            val intent = Intent(context, ConversationActivity::class.java)
                .putExtra("contact", contact)
            context.startActivity(intent)
        }
    }

    override fun getFilter(): Filter {

        return object : Filter() {

            override fun performFiltering(p0: CharSequence?): FilterResults {
                val query = p0.toString()
                val filteredList = if (query.isNotEmpty()) {
                    contacts.filter { it.username?.contains(query, true)!! } as ArrayList<User>
                } else {
                    contacts
                }
                return FilterResults().also { it.values = filteredList }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                filteredContacts = p1!!.values as ArrayList<User>
                notifyDataSetChanged()
            }

        }

    }

    inner class ContactHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var contactUsername: TextView = itemView.findViewById(R.id.contactUsername)
        internal var parentLayout: LinearLayout = itemView.findViewById(R.id.contactLinearLayout)
    }

}