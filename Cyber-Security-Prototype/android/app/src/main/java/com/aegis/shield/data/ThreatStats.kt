package com.aegis.shield.data

data class ThreatStats(
    val totalScanned: Int,
    val smsScanned: Int,
    val callsMonitored: Int,
    val threatsBlocked: Int,
    val smishingThreats: Int,
    val vishingThreats: Int,
    val urlThreats: Int,
    val mediumThreats: Int,
    val criticalThreats: Int,
)

