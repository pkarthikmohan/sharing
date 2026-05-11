package com.aegis.shield.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.shield.data.ThreatBand
import com.aegis.shield.data.ThreatDao
import com.aegis.shield.data.TwilioRepository
import com.aegis.shield.domain.repository.ThreatRepository
import com.aegis.shield.util.AppResult
import com.aegis.shield.util.RiskScoreCalculator
import com.aegis.shield.util.ThreatVectorScore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AlertUiState {

    data object Idle : AlertUiState

    data class Analyzed(
        val riskScore: Int,
        val sender: String,
        val body: String,
        val threatVectors: List<ThreatVectorScore>,
        val band: ThreatBand,
        val explanation: String,
        /** Database row ID when opened from notifications or persisted threat lookup; otherwise 0. */
        val threatId: Long,
        val blocked: Boolean,
        val reported: Boolean,
    ) : AlertUiState
}

private const val REPORT_TO_NUMBER = "+916361892311"

@HiltViewModel
class AlertViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val threatRepository: ThreatRepository,
    private val threatDao: ThreatDao,
    private val twilioRepository: TwilioRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlertUiState>(AlertUiState.Idle)
    val uiState: StateFlow<AlertUiState> = _uiState.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        viewModelScope.launch { resolveInitialState(savedStateHandle) }
    }

    fun consumeToast() {
        _toastMessage.value = null
    }

    private suspend fun resolveInitialState(savedStateHandle: SavedStateHandle) {
        val threatId = savedStateHandle.get<Long>("threatId") ?: 0L
        val argSender = savedStateHandle.get<String>("sender").orEmpty()
        val argBody = savedStateHandle.get<String>("body").orEmpty()

        val entity = if (threatId > 0L) threatRepository.getThreatById(threatId) else null

        val sender = argSender.ifBlank { entity?.sender.orEmpty() }
        val body = argBody.ifBlank { entity?.body.orEmpty() }

        if (sender.isBlank() && body.isBlank()) {
            _uiState.value = AlertUiState.Idle
            return
        }

        val analysis = RiskScoreCalculator.analyze(sender, body.trim())
        _uiState.value = AlertUiState.Analyzed(
            riskScore = analysis.riskScore,
            sender = sender,
            body = body.trim(),
            threatVectors = analysis.threatVectors,
            band = analysis.band,
            explanation = analysis.explanation,
            threatId = entity?.id ?: 0L,
            blocked = entity?.blocked ?: false,
            reported = entity?.reported ?: false,
        )
    }

    fun markBlocked() {
        val s = _uiState.value as? AlertUiState.Analyzed ?: return
        if (s.threatId <= 0L) {
            _uiState.update { curr ->
                if (curr is AlertUiState.Analyzed) curr.copy(blocked = true) else curr
            }
            return
        }
        viewModelScope.launch {
            threatDao.markBlocked(s.threatId)
            _uiState.update { curr ->
                if (curr is AlertUiState.Analyzed) curr.copy(blocked = true) else curr
            }
        }
    }

    fun markReportedLocal() {
        val s = _uiState.value as? AlertUiState.Analyzed ?: return
        if (s.threatId <= 0L) {
            _uiState.update { curr ->
                if (curr is AlertUiState.Analyzed) curr.copy(reported = true) else curr
            }
            return
        }
        viewModelScope.launch {
            threatDao.markReported(s.threatId)
            _uiState.update { curr ->
                if (curr is AlertUiState.Analyzed) curr.copy(reported = true) else curr
            }
        }
    }

    fun reportScamToAuthority(sender: String, body: String) {
        viewModelScope.launch {
            val message =
                "[Aegis Automated Report] Scam detected from $sender. Content: $body"
            when (
                twilioRepository.sendVerificationSms(
                    toNumber = REPORT_TO_NUMBER,
                    message = message,
                )
            ) {
                is AppResult.Success -> {
                    _toastMessage.value = "Threat reported to authorities via SMS"
                    markReportedLocal()
                }
                is AppResult.Error -> {
                    /* prototype: failures are silent besides Twilio wiring */
                }
            }
        }
    }
}
