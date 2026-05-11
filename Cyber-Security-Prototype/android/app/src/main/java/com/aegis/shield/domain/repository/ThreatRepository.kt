package com.aegis.shield.domain.repository

import com.aegis.shield.data.ThreatEntity
import com.aegis.shield.data.ThreatStats
import kotlinx.coroutines.flow.Flow

interface ThreatRepository {
    fun getRecentThreats(limit: Int): Flow<List<ThreatEntity>>
    fun getThreatStats(): Flow<ThreatStats>
    suspend fun insertThreat(threat: ThreatEntity): Long
    suspend fun getThreatById(id: Long): ThreatEntity?
}

