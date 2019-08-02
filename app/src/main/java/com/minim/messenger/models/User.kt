package com.minim.messenger.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.auth.FirebaseUser

data class User(
    val uid: String? = null,
    val username: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null
) : Parcelable {

    constructor(user: FirebaseUser) : this(
        user.uid,
        user.displayName,
        user.phoneNumber,
        user.email
    )

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(username)
        parcel.writeString(phoneNumber)
        parcel.writeString(email)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel) = User(parcel)
        override fun newArray(size: Int) = arrayOfNulls<User>(size)
    }

}