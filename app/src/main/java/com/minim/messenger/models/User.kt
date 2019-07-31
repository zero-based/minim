package com.minim.messenger.models

data class User(
    val uid: String? = null,
    val phoneNumber: String? = null,
    val username: String? = null,
    val contacts: ArrayList<User>? = ArrayList()
)