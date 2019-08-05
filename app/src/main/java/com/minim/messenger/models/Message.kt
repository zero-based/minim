package com.minim.messenger.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class Message(
    var id: String? = null,
    var conversationId: String? = null,
    val sender: String? = null,
    val receiver: String? = null,
    var type: Type? = null,
    val content: String? = null,
    var seen: Boolean? = null,
    val duration: Long? = null,
    val sentOn: Timestamp? = null,
    var seenOn: Timestamp? = null,
    var deleteOn: Timestamp? = null
) : Parcelable {

    enum class Type {
        TO,
        FROM
    }

    fun isOverdue() = deleteOn != null && deleteOn!! < Timestamp.now()

    fun isFromOther(otherUid: String) = sender == otherUid

    fun determineType(otherUid: String) {
        if (isFromOther(otherUid)) {
            type = Type.FROM
        }
    }

    fun markAsSeen() {
        seen = true
        seenOn = Timestamp.now()
        deleteOn = Timestamp(seenOn!!.seconds + duration!!, 0)
    }

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        Type.values()[parcel.readInt()],
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readParcelable(Timestamp::class.java.classLoader),
        parcel.readParcelable(Timestamp::class.java.classLoader),
        parcel.readParcelable(Timestamp::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(conversationId)
        parcel.writeString(sender)
        parcel.writeString(receiver)
        parcel.writeInt(type?.ordinal!!)
        parcel.writeString(content)
        parcel.writeValue(seen)
        parcel.writeValue(duration)
        parcel.writeParcelable(sentOn, flags)
        parcel.writeParcelable(seenOn, flags)
        parcel.writeParcelable(deleteOn, flags)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Message> {
        override fun createFromParcel(parcel: Parcel) = Message(parcel)
        override fun newArray(size: Int) = arrayOfNulls<Message>(size)
    }

}
