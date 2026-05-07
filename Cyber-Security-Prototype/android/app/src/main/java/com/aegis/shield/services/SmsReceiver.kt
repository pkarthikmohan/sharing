package com.aegis.shield.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.aegis.shield.MainActivity
import com.aegis.shield.R
import com.aegis.shield.data.ThreatDatabase
import com.aegis.shield.data.ThreatEntity
import com.aegis.shield.ml.SmishingClassifier
import com.aegis.shield.data.ThreatBand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG          = "SmsReceiver"
        private const val CHANNEL_ID   = "aegis_sms_threats"
        private const val CHANNEL_NAME = "Aegis SMS Threat Alerts"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        // Respect user preference toggles from Settings screen (stored in SharedPreferences by AegisViewModel)
        val prefs = context.getSharedPreferences("aegis_prefs", Context.MODE_PRIVATE)
        val smsEnabled = prefs.getBoolean("sms_enabled", true)
        if (!smsEnabled) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages[0].originatingAddress ?: "Unknown"
        val body   = messages.joinToString("") { it.messageBody }

        Log.d(TAG, "SMS from $sender: ${body.take(80)}")

        // SMS receivers are time-bounded. Use goAsync() so we can safely finish after async work.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val classifier = SmishingClassifier(context)
                val rawResult  = classifier.classify(body)
                classifier.close()

                // Apply sensitivity thresholds chosen in Settings (0=Conservative, 1=Balanced, 2=Aggressive)
                val sensitivity = prefs.getInt("sensitivity", 1)
                val band = classifyBand(rawResult.score, sensitivity)

                Log.d(TAG, "Smishing score=${rawResult.score} band=$band (sensitivity=$sensitivity)")

                // Save every SMS that scores above SAFE
                if (band != ThreatBand.SAFE) {
                    val dao     = ThreatDatabase.getInstance(context).threatDao()
                    val threat  = ThreatEntity(
                        type      = "SMISHING",
                        sender    = sender,
                        body      = body,
                        score     = rawResult.score,
                        band      = band.name,
                    )
                    val id = dao.insert(threat)

                    // Auto-report (optional)
                    val autoReport = prefs.getBoolean("auto_report", false)
                    if (autoReport) {
                        val firestoreRepo = com.aegis.shield.data.FirestoreRepository()
                        firestoreRepo.reportThreat(threat)
                    }

                    // Notify on SUSPICIOUS and above
                    showNotification(context, sender, rawResult.score, band, id)
                }
            } catch (e: Exception) {
                // Common cause: missing model assets (sms_model.tflite / vocab mismatch).
                Log.e(TAG, "Classification failed: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun classifyBand(score: Int, sensitivity: Int): ThreatBand {
        // Conservative flags fewer; aggressive flags more.
        // Mirrors the UI copy shown in Settings:
        // - Conservative: score >= 80
        // - Balanced:     score >= 60
        // - Aggressive:   score >= 40
        val suspiciousMin = when (sensitivity) {
            0 -> 80
            2 -> 40
            else -> 60
        }

        // Keep a little granularity for notifications
        val likelyMin = (suspiciousMin + 15).coerceAtMost(90)
        val confirmedMin = (suspiciousMin + 30).coerceAtMost(95)

        return when {
            score >= confirmedMin -> ThreatBand.CONFIRMED
            score >= likelyMin    -> ThreatBand.LIKELY
            score >= suspiciousMin -> ThreatBand.SUSPICIOUS
            else -> ThreatBand.SAFE
        }
    }

    private fun showNotification(
        context: Context,
        sender: String,
        score: Int,
        band: ThreatBand,
        threatId: Long,
    ) {
        // Android 13+ requires POST_NOTIFICATIONS; if denied, skip notifying but still save.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel once
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        )

        // Deep-link into AlertScreen
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route", "alert/$threatId")
        }
        val pi = PendingIntent.getActivity(
            context, threatId.toInt(), tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when (band) {
            ThreatBand.CONFIRMED  -> "🚨 Confirmed Scam SMS"
            ThreatBand.LIKELY     -> "⚠️ Likely Scam SMS"
            ThreatBand.SUSPICIOUS -> "⚠️ Suspicious SMS"
            ThreatBand.SAFE       -> return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle(title)
            .setContentText("From $sender · Risk score $score/100")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        nm.notify(threatId.toInt(), notification)
    }
}
