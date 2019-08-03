package com.minim.messenger.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class Conversation(
    val id: String? = null,
    val participants: ArrayList<User> = arrayListOf(),
    val messages: ArrayList<Message> = arrayListOf()
) : Parcelable {

    var hasChanges: Boolean? = false
    val user = participants[0]
    val other = participants[1]

    val document = hashMapOf<String, Any>(
        "id" to id.toString(),
        "participants" to arrayListOf<String>().also {
            participants.forEach { p -> it.add(p.uid.toString()) }
        },
        "messages" to arrayListOf<String>().also {
            messages.forEach { m -> it.add(m.id.toString()) }
        }
    )

    fun processMessages() {
        categorizeMessages()
        deleteOverDueMessages()
        markSeenMessages()
        messages.sortBy { it.sentOn }
    }

    private fun categorizeMessages() {
        messages.filter { m ->
            m.sender == other.uid
        }.forEach { m ->
            m.type = Message.Type.FROM
        }
    }

    fun getOtherUnseenMessages(): List<Message> {
        return messages.filter {
            it.sender == other.uid && it.seen == false
        }
    }

    private fun markSeenMessages() {
        getOtherUnseenMessages().forEach {
            it.seen = true
            it.seenOn = Timestamp.now()
        }
    }

    fun getOverDueMessages(): List<Message> {
        return messages.filter {
            it.deleteOn != null && it.deleteOn!! < Timestamp.now()
        }
    }

    private fun deleteOverDueMessages() {
        getOverDueMessages().forEach { messages.remove(it) }
    }

    fun getMessageIndex(messageId: String): Int {
        return messages.indexOfFirst {
            it.id == messageId
        }

    }

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        arrayListOf<User>().apply {
            parcel.readList(this as List<*>, User::class.java.classLoader)
        },
        arrayListOf<Message>().apply {
            parcel.readList(this as List<*>, Message::class.java.classLoader)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeList(participants as List<*>?)
        parcel.writeList(messages as List<*>?)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Conversation> {
        override fun createFromParcel(parcel: Parcel) = Conversation(parcel)
        override fun newArray(size: Int) = arrayOfNulls<Conversation>(size)
    }

}