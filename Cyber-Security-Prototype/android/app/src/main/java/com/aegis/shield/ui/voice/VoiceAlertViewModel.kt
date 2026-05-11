package com.aegis.shield.ui.voice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telecom.TelecomManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.aegis.shield.util.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class VoiceAlertViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val callState: StateFlow<CallAnalysisState> = LiveCallStateStore.state

    @Suppress("DEPRECATION")
    fun endActiveCall(): AppResult<Unit> {
        return try {
            ActiveCallHolder.activeCall?.let { call ->
                call.disconnect()
                return AppResult.Success(Unit)
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                return AppResult.Error("End call requires Android 9+ support in this app path.")
            }
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ANSWER_PHONE_CALLS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                return AppResult.Error("ANSWER_PHONE_CALLS permission is not granted.")
            }

            val telecomManager = context.getSystemService(TelecomManager::class.java)
            if (telecomManager == null) {
                AppResult.Error("TelecomManager unavailable on this device.")
            } else if (telecomManager.endCall()) {
                AppResult.Success(Unit)
            } else {
                AppResult.Error("Unable to end call. App may need dialer privileges.")
            }
        } catch (e: SecurityException) {
            AppResult.Error("Security error while ending call: ${e.message ?: "Permission denied"}")
        } catch (e: Exception) {
            AppResult.Error("Failed to end call: ${e.message ?: "Unknown error"}")
        }
    }
}
