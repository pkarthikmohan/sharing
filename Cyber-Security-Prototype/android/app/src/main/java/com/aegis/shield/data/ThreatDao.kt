package com.aegis.shield.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreatDao {
    @Query("SELECT * FROM threats ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<ThreatEntity>>

    @Query("SELECT * FROM threats ORDER BY timestamp DESC LIMIT 5")
    fun getRecentFlow(): Flow<List<ThreatEntity>>

    @Query("SELECT * FROM threats WHERE timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getSince(since: Long): List<ThreatEntity>

    @Query("SELECT COUNT(*) FROM threats WHERE type = 'SMISHING'")
    fun smsScannedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM threats WHERE type = 'VISHING'")
    fun callsMonitoredCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM threats WHERE band = 'CONFIRMED' OR band = 'LIKELY'")
    fun threatsBlockedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(threat: ThreatEntity): Long

    @Query("UPDATE threats SET blocked = 1 WHERE id = :id")
    suspend fun markBlocked(id: Long)

    @Query("UPDATE threats SET reported = 1 WHERE id = :id")
    suspend fun markReported(id: Long)

    @Query("DELETE FROM threats")
    suspend fun deleteAll()
}
