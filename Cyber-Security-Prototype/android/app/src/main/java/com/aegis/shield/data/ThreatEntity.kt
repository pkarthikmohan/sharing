package com.aegis.shield.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ThreatBand { CONFIRMED, LIKELY, SUSPICIOUS, SAFE }
enum class ThreatType { SMISHING, VISHING, URL_SCAM }

@Entity(
    tableName = "threats",
    indices = [
        Index(
            value = ["sender", "timestamp", "body"],
            unique = true,
        ),
    ],
)
data class ThreatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: String,           // ThreatType.name
    val sender: String,
    val body: String?,
    val score: Int,             // 0–100
    val band: String,           // ThreatBand.name
    val timestamp: Long = System.currentTimeMillis(),
    val blocked: Boolean = false,
    val reported: Boolean = false,
)
