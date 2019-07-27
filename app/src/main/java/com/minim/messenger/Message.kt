package com.minim.messenger

import java.sql.Timestamp

data class Message(
    val id: String,
    val senderUid: String,
    val receiverUid: String,
    val content: String,
    val isSeen: Boolean,
    val duration: Int,
    val sent: Timestamp,
    val seen: Timestamp
)