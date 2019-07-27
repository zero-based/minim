package com.minim.messenger.models

import com.google.firebase.Timestamp

data class Message(
    val id: String,
    val senderUid: String,
    val receiverUid: String,
    val type: MessageType,
    val content: String,
    val isSeen: Boolean,
    val duration: Int,
    val sent: Timestamp,
    val seen: Timestamp
)

enum class MessageType(val value: Int) {
    TO(0),
    FROM(1)
}