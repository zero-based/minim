package com.minim.messenger.models

import com.google.firebase.Timestamp

data class Message(
    val id: String? = null,
    val senderUid: String? = null,
    val receiverUid: String? = null,
    val type: Type? = null,
    val content: String? = null,
    val isSeen: Boolean? = null,
    val duration: Int? = null,
    val sent: Timestamp? = null,
    val seen: Timestamp? = null
) {
    enum class Type {
        TO,
        FROM
    }
}
