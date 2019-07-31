package com.minim.messenger.adaptors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.minim.messenger.R
import com.minim.messenger.models.Message
import com.minim.messenger.models.MessageType

class ConversationAdaptor(private val messages: ArrayList<Message>) :
    RecyclerView.Adapter<ConversationAdaptor.MessageHolder>() {

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int) = messages[position].type.value

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {

        val layoutType = when (viewType) {
            MessageType.TO.value -> R.layout.item_message_from
            MessageType.FROM.value -> R.layout.item_message_to
            else -> -1
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutType, parent, false)
        return MessageHolder(view)
    }

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        holder.userMessage.text = messages[position].content
    }

    inner class MessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var userMessage: TextView = itemView.findViewById(R.id.userMessage)
    }


}
