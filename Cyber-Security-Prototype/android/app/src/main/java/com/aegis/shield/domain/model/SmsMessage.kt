package com.aegis.shield.domain.model

data class SmsMessage(
    val sender: String,
    val body: String,
    val timestamp: Long,
    val threadId: Long,
)
