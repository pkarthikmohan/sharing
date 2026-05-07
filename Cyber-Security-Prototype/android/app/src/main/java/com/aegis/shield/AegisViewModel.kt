package com.aegis.shield

import android.app.Application
import android.content.Context
import android.provider.CallLog
import android.provider.Telephony
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.shield.data.CallAnalysis
import com.aegis.shield.data.CallLogEntry
import com.aegis.shield.data.SmsAnalysis
import com.aegis.shield.data.SmsBox
import com.aegis.shield.data.SmsMessage
import com.aegis.shield.data.ThreatBand
import com.aegis.shield.data.ThreatDatabase
import com.aegis.shield.data.ThreatEntity
import com.aegis.shield.data.TrustedContact
import com.aegis.shield.ml.SmishingClassifier
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    // ── Trusted contacts (Safe-Word) ──────────────────────────
    private val _trusted = MutableStateFlow(loadTrustedContacts())
    val trustedContacts: StateFlow<List<TrustedContact>> = _trusted.asStateFlow()

    private fun loadTrustedContacts(): List<TrustedContact> {
        val raw = prefs.getString("trusted_contacts_v1", "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split('\n')
            .mapNotNull { line ->
                val parts = line.split('\u001F')
                if (parts.size != 3) null else TrustedContact(parts[0], parts[1], parts[2])
            }
    }

    private fun saveTrustedContacts(list: List<TrustedContact>) {
        val raw = list.joinToString("\n") { "${it.name}\u001F${it.relation}\u001F${it.number}" }
        prefs.edit().putString("trusted_contacts_v1", raw).apply()
        _trusted.value = list
    }

    fun addTrustedContact(name: String, relation: String, number: String) {
        val n = name.trim()
        val r = relation.trim().ifBlank { "Contact" }
        val num = number.trim()
        if (n.isBlank() || num.isBlank()) return
        val next = (trustedContacts.value + TrustedContact(n, r, num)).distinctBy { it.number }
        saveTrustedContacts(next)
    }

    fun removeTrustedContact(number: String) {
        val next = trustedContacts.value.filterNot { it.number == number }
        saveTrustedContacts(next)
    }

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

    // ── Device data (SMS + Call Logs) ─────────────────────────
    private val _sms = MutableStateFlow<List<SmsAnalysis>>(emptyList())
    val sms: StateFlow<List<SmsAnalysis>> = _sms.asStateFlow()

    private val _calls = MutableStateFlow<List<CallAnalysis>>(emptyList())
    val calls: StateFlow<List<CallAnalysis>> = _calls.asStateFlow()

    fun refreshSms(limit: Int = 80) {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val sensitivity = settings.value.sensitivity
            val list = withContext(Dispatchers.IO) {
                querySms(ctx, limit).mapNotNull { msg ->
                    runCatching {
                        val classifier = SmishingClassifier(ctx)
                        val result = classifier.classify(msg.body)
                        classifier.close()
                        val band = bandForScore(result.score, sensitivity)
                        SmsAnalysis(
                            msg = msg,
                            score = result.score,
                            band = band,
                            otpCode = extractOtp(msg.body),
                        )
                    }.getOrNull()
                }
            }
            _sms.value = list.sortedByDescending { it.msg.timestamp }
        }
    }

    fun refreshCallLogs(limit: Int = 200) {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val trustedSet = trustedContacts.value.map { normaliseNumber(it.number) }.toSet()
            val knownBad = allThreats.value
                .filter { it.band == ThreatBand.CONFIRMED.name || it.band == ThreatBand.LIKELY.name }
                .map { normaliseNumber(it.sender) }
                .toSet()

            val list = withContext(Dispatchers.IO) {
                queryCallLogs(ctx, limit).map { e ->
                    val numNorm = normaliseNumber(e.number ?: "")
                    when {
                        numNorm.isNotBlank() && trustedSet.contains(numNorm) ->
                            CallAnalysis(e, ThreatBand.SAFE, 0, "Trusted contact")
                        numNorm.isNotBlank() && knownBad.contains(numNorm) ->
                            CallAnalysis(e, ThreatBand.LIKELY, 75, "Previously flagged number")
                        isSuspiciousNumber(e.number) ->
                            CallAnalysis(e, ThreatBand.SUSPICIOUS, 55, "Unknown / unusual number pattern")
                        else ->
                            CallAnalysis(e, ThreatBand.SAFE, 0, "No risk signals")
                    }
                }
            }
            _calls.value = list.sortedByDescending { it.entry.timestamp }
        }
    }

    private fun querySms(ctx: Context, limit: Int): List<SmsMessage> {
        val cr = ctx.contentResolver
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
        )

        fun read(uri: android.net.Uri, box: SmsBox): List<SmsMessage> {
            val out = mutableListOf<SmsMessage>()
            cr.query(uri, projection, null, null, "${Telephony.Sms.DATE} DESC")?.use { c ->
                val idCol = c.getColumnIndexOrThrow(Telephony.Sms._ID)
                val addrCol = c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyCol = c.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateCol = c.getColumnIndexOrThrow(Telephony.Sms.DATE)
                var count = 0
                while (c.moveToNext() && count < limit) {
                    val body = c.getString(bodyCol) ?: ""
                    out += SmsMessage(
                        id = c.getLong(idCol),
                        address = c.getString(addrCol),
                        body = body,
                        timestamp = c.getLong(dateCol),
                        box = box,
                    )
                    count++
                }
            }
            return out
        }

        // Read inbox + sent, then merge
        return (read(Telephony.Sms.Inbox.CONTENT_URI, SmsBox.INBOX) +
                read(Telephony.Sms.Sent.CONTENT_URI, SmsBox.SENT))
    }

    private fun queryCallLogs(ctx: Context, limit: Int): List<CallLogEntry> {
        val cr = ctx.contentResolver
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
        )
        val out = mutableListOf<CallLogEntry>()
        cr.query(CallLog.Calls.CONTENT_URI, projection, null, null, "${CallLog.Calls.DATE} DESC")?.use { c ->
            val idCol = c.getColumnIndexOrThrow(CallLog.Calls._ID)
            val numCol = c.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
            val nameCol = c.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)
            val typeCol = c.getColumnIndexOrThrow(CallLog.Calls.TYPE)
            val dateCol = c.getColumnIndexOrThrow(CallLog.Calls.DATE)
            val durCol = c.getColumnIndexOrThrow(CallLog.Calls.DURATION)
            var count = 0
            while (c.moveToNext() && count < limit) {
                out += CallLogEntry(
                    id = c.getLong(idCol),
                    number = c.getString(numCol),
                    cachedName = c.getString(nameCol),
                    type = c.getInt(typeCol),
                    timestamp = c.getLong(dateCol),
                    durationSec = c.getLong(durCol),
                )
                count++
            }
        }
        return out
    }

    private fun bandForScore(score: Int, sensitivity: Int): ThreatBand {
        val suspiciousMin = when (sensitivity) {
            0 -> 80
            2 -> 40
            else -> 60
        }
        val likelyMin = (suspiciousMin + 15).coerceAtMost(90)
        val confirmedMin = (suspiciousMin + 30).coerceAtMost(95)
        return when {
            score >= confirmedMin -> ThreatBand.CONFIRMED
            score >= likelyMin -> ThreatBand.LIKELY
            score >= suspiciousMin -> ThreatBand.SUSPICIOUS
            else -> ThreatBand.SAFE
        }
    }

    private fun extractOtp(body: String): String? {
        val hasOtpKeyword = Regex("\\b(otp|one\\s*time\\s*password|verification\\s*code|auth\\s*code)\\b", RegexOption.IGNORE_CASE).containsMatchIn(body)
        if (!hasOtpKeyword) return null
        return Regex("\\b\\d{4,8}\\b").find(body)?.value
    }

    private fun normaliseNumber(n: String): String =
        n.filter { it.isDigit() }.takeLast(12) // keep last digits to ignore country prefix differences

    private fun isSuspiciousNumber(n: String?): Boolean {
        val raw = n?.trim().orEmpty()
        if (raw.isBlank()) return true
        // Unknown/private numbers often show up as "Unknown", "-1", etc.
        if (!raw.any { it.isDigit() }) return true
        val digits = raw.filter { it.isDigit() }
        // Too short or extremely long is suspicious
        if (digits.length < 8 || digits.length > 15) return true
        // Excessive repeated digits (e.g., 0000000000)
        if (Regex("^(\\d)\\1{7,}$").matches(digits)) return true
        return false
    }
}
