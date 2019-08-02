package com.minim.messenger.adapters

import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
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
            val firestore = FirebaseFirestore.getInstance()

            val docRef = firestore.collection("conversations").document(conversation.id)

            docRef.get().addOnCompleteListener {

                val messagesIds = it.result!!["messages"] as ArrayList<String>
                if (messagesIds.isEmpty()) {
                    startConversation(conversation)
                    return@addOnCompleteListener
                }
                for ((i, id) in messagesIds.withIndex()) {
                    firestore.collection("messages").document(id)
                        .get().addOnSuccessListener { doc ->
                            conversation.messages.add(doc.toObject(Message::class.java)!!)
                        }.addOnCompleteListener {
                            if (i == messagesIds.lastIndex) {
                                val unseenMessages = conversation.getOtherUnseenMessages()
                                unseenMessages.forEach { m ->
                                    m.seen = true
                                    m.seenOn = Timestamp.now()
                                    firestore.collection("messages")
                                        .document(m.id!!)
                                        .set(m)
                                }
                                conversation.markSeenMessages()
                                conversation.processMessages()
                                dismissExistingNotification(holder, position)
                                startConversation(conversation)
                            }
                        }
                }
            }

        }
    }

    private fun startConversation(conversation: Conversation) {
        val intent = Intent(context, ConversationLogActivity::class.java)
        intent.putExtra("conversation", conversation)
        context.startActivity(intent)
    }

    private fun dismissExistingNotification(holder: ContactHolder, position: Int) {
        ConversationsActivity.currentConversationIndex = position
        holder.hasChanges.visibility = View.GONE

        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(position)
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