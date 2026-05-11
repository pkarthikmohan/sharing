package com.aegis.shield.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreatDao {
    @Query("SELECT * FROM threats ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<ThreatEntity>>

    @Query("SELECT * FROM threats ORDER BY timestamp DESC LIMIT 5")
    fun getRecentFlow(): Flow<List<ThreatEntity>>

    @Query("SELECT * FROM threats WHERE band != 'SAFE' ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentThreats(limit: Int): Flow<List<ThreatEntity>>

    @Query(
        """
        SELECT * FROM threats
        WHERE type = 'SMISHING' AND score >= :minScore
        ORDER BY timestamp DESC LIMIT :limit
        """
    )
    fun getRecentSmsThreatsByMinScore(minScore: Int, limit: Int): Flow<List<ThreatEntity>>

    @Query("SELECT * FROM threats WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ThreatEntity?

    @Query(
        """
        SELECT
            COUNT(*) AS totalScanned,
            SUM(CASE WHEN type = 'SMISHING' THEN 1 ELSE 0 END) AS smsScanned,
            SUM(CASE WHEN type = 'VISHING' THEN 1 ELSE 0 END) AS callsMonitored,
            SUM(CASE WHEN band IN ('CONFIRMED','LIKELY') THEN 1 ELSE 0 END) AS threatsBlocked,
            SUM(CASE WHEN type = 'SMISHING' AND band != 'SAFE' THEN 1 ELSE 0 END) AS smishingThreats,
            SUM(CASE WHEN type = 'VISHING' AND band != 'SAFE' THEN 1 ELSE 0 END) AS vishingThreats,
            SUM(CASE WHEN type = 'URL_SCAM' AND band != 'SAFE' THEN 1 ELSE 0 END) AS urlThreats,
            SUM(CASE WHEN band IN ('SUSPICIOUS','LIKELY') THEN 1 ELSE 0 END) AS mediumThreats,
            SUM(CASE WHEN band = 'CONFIRMED' THEN 1 ELSE 0 END) AS criticalThreats
        FROM threats
        """
    )
    fun getThreatStats(): Flow<ThreatStats>

    @Query("SELECT * FROM threats WHERE timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getSince(since: Long): List<ThreatEntity>

    @Query("SELECT COUNT(*) FROM threats WHERE type = 'SMISHING'")
    fun smsScannedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM threats WHERE type = 'VISHING'")
    fun callsMonitoredCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM threats WHERE band = 'CONFIRMED' OR band = 'LIKELY'")
    fun threatsBlockedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(threat: ThreatEntity): Long

    @Query("UPDATE threats SET blocked = 1 WHERE id = :id")
    suspend fun markBlocked(id: Long)

    @Query("UPDATE threats SET reported = 1 WHERE id = :id")
    suspend fun markReported(id: Long)

    @Query("DELETE FROM threats")
    suspend fun deleteAll()
}
