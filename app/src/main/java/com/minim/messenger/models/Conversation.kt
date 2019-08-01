package com.minim.messenger.models

import android.os.Parcel
import android.os.Parcelable

data class Conversation(
    var participant_1: User? = null,
    var participant_2: User? = null,
    val messages: ArrayList<Message>? = arrayListOf()
) : Parcelable {

    var id = generateID()

    private fun generateID(): String {
        val firstUsername = participant_1!!.username!!
        val secondUsername = participant_2!!.username!!
        return if (firstUsername < secondUsername) {
            "$firstUsername:$secondUsername"
        } else {
            "$secondUsername:$firstUsername"
        }
    }

    val emptyDocument = hashMapOf(
        "id" to id,
        "participant_1" to participant_1!!.username,
        "participant_2" to participant_2!!.username,
        "messages" to arrayListOf<Message>()
    )

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(User::class.java.classLoader),
        parcel.readParcelable(User::class.java.classLoader),
        arrayListOf<Message>().apply {
            parcel.readList(this as List<*>, Message::class.java.classLoader)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(participant_1, flags)
        parcel.writeParcelable(participant_2, flags)
        parcel.writeList(messages as List<*>?)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Conversation> {
        override fun createFromParcel(parcel: Parcel) = Conversation(parcel)
        override fun newArray(size: Int) = arrayOfNulls<Conversation>(size)
    }

}