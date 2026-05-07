package com.aegis.shield.data

data class SmsMessage(
    val id: Long,
    val address: String?,
    val body: String,
    val timestamp: Long,
    val box: SmsBox,
)

enum class SmsBox { INBOX, SENT }

data class SmsAnalysis(
    val msg: SmsMessage,
    val score: Int,
    val band: ThreatBand,
    val otpCode: String?,
)

data class CallLogEntry(
    val id: Long,
    val number: String?,
    val cachedName: String?,
    val type: Int,
    val timestamp: Long,
    val durationSec: Long,
)

data class CallAnalysis(
    val entry: CallLogEntry,
    val band: ThreatBand,
    val score: Int,
    val reason: String,
)

data class TrustedContact(
    val name: String,
    val relation: String,
    val number: String,
)

