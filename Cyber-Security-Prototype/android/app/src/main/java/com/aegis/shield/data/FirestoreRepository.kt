package com.aegis.shield.data

import android.util.Log
import com.aegis.shield.data.ThreatEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Handles all interactions with Firebase Firestore.
 */
class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val threatsCollection = db.collection("global_threats")

    companion object {
        private const val TAG = "FirestoreRepository"
    }

    /**
     * Reports a detected threat to the centralized cloud database.
     * This helps build crowdsourced threat intelligence.
     */
    suspend fun reportThreat(threat: ThreatEntity) {
        try {
            val threatData = hashMapOf(
                "type" to threat.type,
                "sender" to threat.sender,
                "score" to threat.score,
                "band" to threat.band,
                "timestamp" to threat.timestamp,
                "reportedAt" to System.currentTimeMillis()
            )

            // Submit to Firestore using Coroutines
            val documentReference = threatsCollection.add(threatData).await()
            Log.d(TAG, "Successfully reported threat. Doc ID: ${documentReference.id}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to report threat to Firestore", e)
        }
    }

    /**
     * Example: Fetch recent global threats identified by other users.
     */
    suspend fun getGlobalThreats(): List<Map<String, Any>> {
        return try {
            val snapshot = threatsCollection
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching global threats", e)
            emptyList()
        }
    }
}
