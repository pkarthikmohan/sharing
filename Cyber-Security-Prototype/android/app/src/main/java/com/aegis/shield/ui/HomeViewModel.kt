package com.aegis.shield.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.shield.data.ThreatEntity
import com.aegis.shield.data.ThreatStats
import com.aegis.shield.data.ThreatSyncManager
import com.aegis.shield.domain.repository.ThreatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val threatRepository: ThreatRepository,
    private val threatSyncManager: ThreatSyncManager,
) : ViewModel() {
    val threatStats: StateFlow<ThreatStats> =
        threatRepository.getThreatStats().stateIn(
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

    val recentThreats: StateFlow<List<ThreatEntity>> =
        threatRepository.getRecentThreats(limit = 10).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )

    init {
        // Quick initial sweep for demo: last 50 messages, only store risk>40
        viewModelScope.launch {
            runCatching { threatSyncManager.syncInitialThreats(limit = 50) }
        }
    }
}

