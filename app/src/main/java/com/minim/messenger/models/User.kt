package com.minim.messenger.models

import android.os.Parcel
import android.os.Parcelable

data class User(
    val uid: String? = null,
    val phoneNumber: String? = null,
    val username: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(phoneNumber)
        parcel.writeString(username)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel) = User(parcel)
        override fun newArray(size: Int) = arrayOfNulls<User>(size)
    }

}