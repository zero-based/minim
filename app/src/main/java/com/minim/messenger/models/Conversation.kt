package com.minim.messenger.models

import android.os.Parcel
import android.os.Parcelable

data class Conversation(
    val id: String? = null,
    var secret: String? = null,
    val participants: ArrayList<User> = arrayListOf(),
    val messages: ArrayList<Message> = arrayListOf()
) : Parcelable {

    var hasChanges: Boolean? = false
    val user = participants[0]
    var other = participants[1]

    val document = hashMapOf<String, Any>(
        "id" to id.toString(),
        "secret" to secret.toString(),
        "participants" to arrayListOf<String>().also {
            participants.forEach { p -> it.add(p.uid.toString()) }
        },
        "messages" to arrayListOf<String>().also {
            messages.forEach { m -> it.add(m.id.toString()) }
        }
    )

    @Suppress("UNCHECKED_CAST")
    constructor(map: Map<String, *>, currentUser: User) : this(
        map["id"] as String?,
        map["secret"] as String?,
        constructParticipants(map["participants"] as ArrayList<String>, currentUser)
    )

    fun initOther(otherUser: User) {
        participants[1] = otherUser
        other = participants[1]
    }

    fun getMessageIndex(messageId: String) = messages.indexOfFirst { it.id == messageId }

    constructor(parcel: Parcel) : this(
        parcel.readString(),
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
        parcel.writeString(secret)
        parcel.writeList(participants as List<*>?)
        parcel.writeList(messages as List<*>?)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Conversation> {

        override fun createFromParcel(parcel: Parcel) = Conversation(parcel)
        override fun newArray(size: Int) = arrayOfNulls<Conversation>(size)

        private fun constructParticipants(
            participantsUids: ArrayList<String>,
            currentUser: User
        ): ArrayList<User> {
            return arrayListOf(
                currentUser,
                User(participantsUids.find { it != currentUser.uid })
            )
        }

    }

}