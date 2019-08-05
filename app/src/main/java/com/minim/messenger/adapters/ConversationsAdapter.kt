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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.minim.messenger.R
import com.minim.messenger.activities.ConversationLogActivity
import com.minim.messenger.activities.ConversationsActivity
import com.minim.messenger.models.Conversation
import com.minim.messenger.models.Message

class ConversationsAdapter(private val context: Context, val conversations: ArrayList<Conversation>) :
    RecyclerView.Adapter<ConversationsAdapter.ContactHolder>(), Filterable {

    private var filteredConversations = conversations
    private val firestore = FirebaseFirestore.getInstance()

    override fun getItemCount() = filteredConversations.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ContactHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_conversation, parent, false))

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {

        val conversation = filteredConversations[position]
        holder.otherUsername.text = conversation.other.username

        if (conversation.hasChanges!!) {
            holder.hasChanges.visibility = View.VISIBLE
        } else {
            holder.hasChanges.visibility = View.GONE
        }

        holder.parentLayout.setOnClickListener {

            conversation.messages.clear()

            firestore.collection("messages")
                .whereEqualTo("conversationId", conversation.id)
                .orderBy("sentOn")
                .get()
                .addOnSuccessListener {
                    it.documents.forEach { doc ->
                        val message = doc.toObject(Message::class.java)!!
                        if (message.isOverdue()) {
                            deleteMessage(conversation, message)
                        } else {
                            updateMessageSeen(conversation, message)
                            message.determineType(conversation.other.uid!!)
                            conversation.messages.add(message)
                        }
                    }
                }.addOnCompleteListener {
                    dismissExistingNotification(holder, position)
                    startConversation(conversation)
                }

        }

    }

    private fun deleteMessage(conversation: Conversation, message: Message) {
        firestore.collection("messages").document(message.id!!).delete()
        firestore.collection("conversations").document(conversation.id!!)
            .update("messages", FieldValue.arrayRemove(message.id))
    }

    private fun updateMessageSeen(conversation: Conversation, message: Message) {
        if (message.seen!! || !message.isFromOther(conversation.other.uid!!)) return
        message.markAsSeen()
        firestore.collection("messages").document(message.id!!).set(message)
    }

    private fun dismissExistingNotification(holder: ContactHolder, position: Int) {
        ConversationsActivity.currentConversationIndex = position
        holder.hasChanges.visibility = View.GONE
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(position)
    }

    private fun startConversation(conversation: Conversation) {
        val intent = Intent(context, ConversationLogActivity::class.java)
        intent.putExtra("conversation", conversation)
        context.startActivity(intent)
    }

    override fun getFilter() = filteringStrategy

    private val filteringStrategy: Filter = object : Filter() {

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

    inner class ContactHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var otherUsername: TextView = itemView.findViewById(R.id.other_username)
        internal var hasChanges: ImageView = itemView.findViewById(R.id.notification_blink)
        internal var parentLayout: LinearLayout = itemView.findViewById(R.id.contact_linear_layout)
    }

}