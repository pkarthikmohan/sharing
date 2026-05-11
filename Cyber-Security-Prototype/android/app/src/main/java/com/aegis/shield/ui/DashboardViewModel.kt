package com.aegis.shield.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.shield.data.ThreatDao
import com.aegis.shield.data.ThreatEntity
import com.aegis.shield.data.ThreatStats
import com.aegis.shield.data.ThreatType
import com.aegis.shield.util.PdfReportGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val threatDao: ThreatDao,
) : ViewModel() {
    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    val threatStats: StateFlow<ThreatStats> =
        threatDao.getThreatStats().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ThreatStats(
                totalScanned = 0,
                smsScanned = 0,
                callsMonitored = 0,
                threatsBlocked = 0,
                smishingThreats = 0,
                vishingThreats = 0,
                urlThreats = 0,
                mediumThreats = 0,
                criticalThreats = 0,
            ),
        )

    val allThreats: StateFlow<List<ThreatEntity>> =
        threatDao.getAllFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topBlockedSenders: StateFlow<List<Pair<String, Int>>> =
        allThreats
            .map { list ->
                list.filter { it.band != "SAFE" }
                    .groupingBy { it.sender }
                    .eachCount()
                    .entries
                    .sortedByDescending { it.value }
                    .take(3)
                    .map { it.key to it.value }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val breakdownPercentages: StateFlow<Map<String, Float>> =
        allThreats
            .map { list ->
                val filtered = list.filter { it.band != "SAFE" }
                val total = filtered.size.coerceAtLeast(1)
                val smishing = filtered.count { it.type == ThreatType.SMISHING.name }
                val vishing = filtered.count { it.type == ThreatType.VISHING.name }
                val url = filtered.count { it.type == ThreatType.URL_SCAM.name }
                mapOf(
                    "SMISHING" to (smishing.toFloat() / total),
                    "VISHING" to (vishing.toFloat() / total),
                    "URL_SCAM" to (url.toFloat() / total),
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun exportReportToPdf(context: Context, uri: Uri) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val allThreats = threatDao.getSince(0L)
                    val blocked = allThreats.count { it.band == "CONFIRMED" || it.band == "LIKELY" }
                    PdfReportGenerator.generate(
                        contentResolver = context.contentResolver,
                        uri = uri,
                        threats = allThreats,
                        totalBlocked = blocked,
                    )
                }
            }.onSuccess {
                _events.emit("Report saved successfully!")
            }.onFailure {
                _events.emit("Error generating report: ${it.message ?: "Unknown error"}")
            }
        }
    }
}
