package com.aegis.shield.services

import android.app.*
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.aegis.shield.MainActivity
import com.aegis.shield.R
import com.aegis.shield.data.ThreatDatabase
import com.aegis.shield.data.ThreatEntity
import com.aegis.shield.ml.VoiceClassifier
import kotlinx.coroutines.*

class CallAudioService : Service() {

    companion object {
        private const val TAG           = "CallAudioService"
        private const val CHANNEL_ID    = "aegis_call_service"
        private const val NOTIF_ID      = 1001
        private const val DEEPFAKE_THRESHOLD = 65   // fire alert above this %
        private const val ROLLING_CHUNKS    = 5     // average over N chunks
    }

    private var serviceJob: Job? = null
    private var audioRecord: AudioRecord? = null
    private var callerName: String = "Incoming Call"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra("CALLER_NAME")?.let { callerName = it }
        
        startForeground(NOTIF_ID, buildForegroundNotification())
        serviceJob = CoroutineScope(Dispatchers.IO).launch { runAnalysisLoop() }
        return START_STICKY
    }

    private suspend fun runAnalysisLoop() {
        val classifier = VoiceClassifier(this)
        val bufSize = maxOf(
            AudioRecord.getMinBufferSize(
                VoiceClassifier.SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ),
            VoiceClassifier.CHUNK_SAMPLES * 2
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            VoiceClassifier.SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufSize
        )

        audioRecord?.startRecording()
        Log.d(TAG, "Audio recording started")

        val recentProbs = ArrayDeque<Int>(ROLLING_CHUNKS)

        while (currentCoroutineContext().isActive) {
            val pcm = ShortArray(VoiceClassifier.CHUNK_SAMPLES)
            val read = audioRecord?.read(pcm, 0, pcm.size) ?: break
            if (read <= 0) continue

            try {
                val result = classifier.analyzeChunk(pcm)
                recentProbs.addLast(result.deepfakeProbability)
                if (recentProbs.size > ROLLING_CHUNKS) recentProbs.removeFirst()

                val rollingAvg = recentProbs.average().toInt()
                Log.d(TAG, "Deepfake rolling avg = $rollingAvg%")

                if (rollingAvg >= DEEPFAKE_THRESHOLD && recentProbs.size == ROLLING_CHUNKS) {
                    withContext(Dispatchers.IO) {
                        val dao = ThreatDatabase.getInstance(this@CallAudioService).threatDao()
                        val threat = ThreatEntity(
                            type   = "VISHING",
                            sender = callerName,
                            body   = null,
                            score  = rollingAvg,
                            band   = "LIKELY",
                        )
                        val id  = dao.insert(threat)
                        
                        // Push Voice Threat to Global Cloud Database
                        val firestoreRepo = com.aegis.shield.data.FirestoreRepository()
                        firestoreRepo.reportThreat(threat)
                        
                        fireVoiceAlert(rollingAvg, id)
                    }
                    recentProbs.clear()   // reset to avoid repeated alerts
                }
            } catch (e: Exception) {
                Log.e(TAG, "Voice analysis error: ${e.message}", e)
            }
        }

        audioRecord?.stop()
        audioRecord?.release()
        classifier.close()
    }

    private fun fireVoiceAlert(prob: Int, threatId: Long) {
        val nm = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel("aegis_voice", "Aegis Voice Alerts", NotificationManager.IMPORTANCE_HIGH)
        )
        val pi = PendingIntent.getActivity(
            this, threatId.toInt(),
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("route", "voice_alert/$threatId")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        nm.notify(threatId.toInt(),
            NotificationCompat.Builder(this, "aegis_voice")
                .setSmallIcon(R.drawable.ic_shield)
                .setContentTitle("🛡️ AI Voice Detected")
                .setContentText("$prob% probability — caller may be AI-generated")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build()
        )
    }

    private fun buildForegroundNotification(): Notification {
        val nm = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Aegis Call Analysis", NotificationManager.IMPORTANCE_LOW)
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle("Aegis is analyzing the call")
            .setContentText("Listening for AI-generated voice patterns…")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        serviceJob?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
