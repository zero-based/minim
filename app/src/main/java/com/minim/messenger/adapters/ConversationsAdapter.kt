package com.minim.messenger.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.minim.messenger.R
import com.minim.messenger.activities.ConversationLogActivity
import com.minim.messenger.activities.ConversationsActivity
import com.minim.messenger.models.Conversation
import com.minim.messenger.models.Message

class ConversationsAdapter(private val context: Context, val conversations: ArrayList<Conversation>) :
    RecyclerView.Adapter<ConversationsAdapter.ContactHolder>(), Filterable {

    private var filteredConversations = conversations

    override fun getItemCount() = filteredConversations.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ContactHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_conversation, parent, false))

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: ContactHolder, position: Int) {

        val conversation = filteredConversations[position]
        holder.otherUsername.text = conversation.other.username

        if (conversation.hasChanges!!) {
            holder.hasChanges.visibility = View.VISIBLE
        } else {
            holder.hasChanges.visibility = View.GONE
        }

        holder.parentLayout.setOnClickListener {


            FirebaseFirestore.getInstance()
                .collection("conversations")
                .document(conversation.id)
                .get().addOnCompleteListener {

                    val messages = it.result!!["messages"] as ArrayList<HashMap<String, *>>
                    messages.forEach { m ->
                        conversation.messages.add(Message(m))
                    }

                    conversation.processMessages()
                    ConversationsActivity.currentConversationIndex = position
                    holder.hasChanges.visibility = View.GONE

                    val intent = Intent(context, ConversationLogActivity::class.java)
                    intent.putExtra("conversation", conversation)
                    context.startActivity(intent)
                }
        }
    }

    override fun getFilter(): Filter {

        return object : Filter() {

            override fun performFiltering(p0: CharSequence?): FilterResults {
                val query = p0.toString()
                val filteredList = if (query.isNotEmpty()) {
                    conversations.filter { it.other.username?.contains(query, true)!! } as ArrayList<Conversation>
                } else {
                    conversations
                }
                return FilterResults().also { it.values = filteredList }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                filteredConversations = p1!!.values as ArrayList<Conversation>
                notifyDataSetChanged()
            }

        }

    }

    inner class ContactHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var otherUsername: TextView = itemView.findViewById(R.id.other_username)
        internal var hasChanges: ImageView = itemView.findViewById(R.id.notification_blink)
        internal var parentLayout: LinearLayout = itemView.findViewById(R.id.contact_linear_layout)
    }

}