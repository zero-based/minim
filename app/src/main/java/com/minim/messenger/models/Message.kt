package com.minim.messenger.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class Message(
    val sender: String? = null,
    val receiver: String? = null,
    var type: Type? = null,
    val content: String? = null,
    val isRead: Boolean? = null,
    val duration: Long? = null,
    val sent: Timestamp? = null,
    val seen: Timestamp? = null
) : Parcelable {

    enum class Type {
        TO,
        FROM
    }

    constructor(hashMap: HashMap<*, *>) : this(
        hashMap["sender"].toString(),
        hashMap["receiver"].toString(),
        Type.valueOf(hashMap["type"].toString()),
        hashMap["content"].toString(),
        hashMap["read"] as Boolean,
        hashMap["duration"] as Long,
        hashMap["sent"] as Timestamp,
        hashMap["seen"] as Timestamp
    )

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        Type.values()[parcel.readInt()],
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readParcelable(Timestamp::class.java.classLoader),
        parcel.readParcelable(Timestamp::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(sender)
        parcel.writeString(receiver)
        parcel.writeInt(type?.ordinal!!)
        parcel.writeString(content)
        parcel.writeValue(isRead)
        parcel.writeValue(duration)
        parcel.writeParcelable(sent, flags)
        parcel.writeParcelable(seen, flags)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Message> {
        override fun createFromParcel(parcel: Parcel) = Message(parcel)
        override fun newArray(size: Int) = arrayOfNulls<Message>(size)
    }

}
