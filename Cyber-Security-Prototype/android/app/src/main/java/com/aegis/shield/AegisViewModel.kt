package com.aegis.shield

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.shield.data.ThreatDatabase
import com.aegis.shield.data.ThreatEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AegisSettings(
    val smsEnabled: Boolean       = true,
    val callEnabled: Boolean      = true,
    val autoReport: Boolean       = false,
    val overlayAlerts: Boolean    = true,
    val sensitivity: Int          = 1,       // 0=Conservative 1=Balanced 2=Aggressive
)

class AegisViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("aegis_prefs", Context.MODE_PRIVATE)
    private val dao   = ThreatDatabase.getInstance(app).threatDao()

    // ── Threat streams ────────────────────────────────────────
    val recentThreats: StateFlow<List<ThreatEntity>> = dao.getRecentFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allThreats: StateFlow<List<ThreatEntity>> = dao.getAllFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val smsScanned: StateFlow<Int> = dao.smsScannedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val callsMonitored: StateFlow<Int> = dao.callsMonitoredCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val threatsBlocked: StateFlow<Int> = dao.threatsBlockedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ── Settings ─────────────────────────────────────────────
    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AegisSettings> = _settings.asStateFlow()

    private fun loadSettings() = AegisSettings(
        smsEnabled    = prefs.getBoolean("sms_enabled", true),
        callEnabled   = prefs.getBoolean("call_enabled", true),
        autoReport    = prefs.getBoolean("auto_report", false),
        overlayAlerts = prefs.getBoolean("overlay_alerts", true),
        sensitivity   = prefs.getInt("sensitivity", 1),
    )

    fun updateSettings(s: AegisSettings) {
        _settings.value = s
        prefs.edit()
            .putBoolean("sms_enabled",    s.smsEnabled)
            .putBoolean("call_enabled",   s.callEnabled)
            .putBoolean("auto_report",    s.autoReport)
            .putBoolean("overlay_alerts", s.overlayAlerts)
            .putInt("sensitivity",        s.sensitivity)
            .apply()
    }

    // ── Actions ───────────────────────────────────────────────
    fun markBlocked(id: Long)  = viewModelScope.launch { dao.markBlocked(id) }
    fun markReported(id: Long) = viewModelScope.launch { dao.markReported(id) }
    fun clearHistory()         = viewModelScope.launch { dao.deleteAll() }

    fun getThreat(id: Long): ThreatEntity? =
        allThreats.value.firstOrNull { it.id == id }
}
