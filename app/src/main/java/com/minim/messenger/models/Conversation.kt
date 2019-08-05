package com.minim.messenger.models

import android.os.Parcel
import android.os.Parcelable

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

    fun getMessageIndex(messageId: String) = messages.indexOfFirst { it.id == messageId }

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