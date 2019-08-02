package com.minim.messenger.models

import android.os.Parcel
import android.os.Parcelable

data class Conversation(
    val participants: ArrayList<User> = arrayListOf(),
    val messages: ArrayList<Message> = arrayListOf()
) : Parcelable {

    val id = generateId()

    val document = hashMapOf<String, Any>(
        "id" to id,
        "participants" to arrayListOf<String>().also {
            participants.forEach { p -> it.add(p.username.toString()) }
        },
        "messages" to messages
    )

    private fun generateId(): String {
        return arrayListOf<String>().also {
            participants.sortedBy { user ->
                user.username
            }.forEach { user ->
                it.add(user.username.toString())
            }
        }.joinToString(":")
    }

    fun processMessages() {
        val other = participants[1]
        messages.filter { m ->
            m.sender == other.username
        }.forEach { m ->
            m.type = Message.Type.FROM
        }
    }

    constructor(parcel: Parcel) : this(
        arrayListOf<User>().apply {
            parcel.readList(this as List<*>, User::class.java.classLoader)
        },
        arrayListOf<Message>().apply {
            parcel.readList(this as List<*>, Message::class.java.classLoader)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeList(participants as List<*>?)
        parcel.writeList(messages as List<*>?)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Conversation> {
        override fun createFromParcel(parcel: Parcel) = Conversation(parcel)
        override fun newArray(size: Int) = arrayOfNulls<Conversation>(size)
    }

}