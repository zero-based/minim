package com.minim.messenger.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.minim.messenger.util.Security

data class Message(
    var id: String? = null,
    var conversationId: String? = null,
    val sender: String? = null,
    val receiver: String? = null,
    @get:Exclude @set:Exclude
    var type: Type? = Type.TO,
    var content: String? = null,
    var seen: Boolean? = false,
    val duration: Long? = null,
    val sentOn: Timestamp? = Timestamp.now(),
    var seenOn: Timestamp? = null,
    var deleteOn: Timestamp? = null
) : Parcelable {

    enum class Type {
        TO,
        FROM
    }

    @Exclude
    fun isOverdue() = deleteOn != null && deleteOn!! < Timestamp.now()

    fun isFromOther(otherUid: String) = sender == otherUid

    fun determineType(otherUid: String) {
        type = if (isFromOther(otherUid)) {
            Type.FROM
        } else {
            Type.TO
        }
    }

    fun markAsSeen() {
        seen = true
        seenOn = Timestamp.now()
        deleteOn = Timestamp(seenOn!!.seconds + duration!!, 0)
    }

    fun encrypt() {
        content = Security.encrypt(content!!)
    }

    fun decrypt() {
        content = Security.decrypt(content!!)
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
