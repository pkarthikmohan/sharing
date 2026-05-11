package com.aegis.shield.data.repository

import com.aegis.shield.data.ThreatDao
import com.aegis.shield.data.ThreatEntity
import com.aegis.shield.data.ThreatStats
import com.aegis.shield.domain.repository.ThreatRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class ThreatRepositoryImpl @Inject constructor(
    private val threatDao: ThreatDao,
) : ThreatRepository {
    override fun getRecentThreats(limit: Int): Flow<List<ThreatEntity>> =
        threatDao.getRecentSmsThreatsByMinScore(minScore = 45, limit = limit)

    override fun getThreatStats(): Flow<ThreatStats> =
        threatDao.getThreatStats()

    override suspend fun insertThreat(threat: ThreatEntity): Long =
        threatDao.insert(threat)

    override suspend fun getThreatById(id: Long): ThreatEntity? =
        threatDao.getById(id)
}

