package com.aegis.shield.ui

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.shield.data.remote.api.VirusTotalApiService
import com.aegis.shield.util.VirusTotalConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface SandboxUiState {
    data object Idle : SandboxUiState
    data object Scanning : SandboxUiState
    data class Safe(
        val url: String,
        val harmless: Int,
        val malicious: Int,
        val suspicious: Int,
    ) : SandboxUiState
    data class Scam(
        val url: String,
        val harmless: Int,
        val malicious: Int,
        val suspicious: Int,
        val flaggedEngines: List<String>,
    ) : SandboxUiState
}

@HiltViewModel
class SandboxViewModel @Inject constructor(
    private val virusTotalApiService: VirusTotalApiService,
) : ViewModel() {
    private val _uiState = MutableStateFlow<SandboxUiState>(SandboxUiState.Idle)
    val uiState: StateFlow<SandboxUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    fun scanUrl(input: String) {
        val url = input.trim()
        if (url.isBlank()) {
            _uiState.value = SandboxUiState.Idle
            return
        }

        val hasScheme = url.startsWith("http://", ignoreCase = true) || url.startsWith("https://", ignoreCase = true)
        val isValid = hasScheme && Patterns.WEB_URL.matcher(url).matches()
        if (!isValid) {
            viewModelScope.launch {
                _events.emit("Invalid URL. Please enter a valid web address including http/https.")
            }
            _uiState.value = SandboxUiState.Idle
            return
        }

        viewModelScope.launch {
            _uiState.value = SandboxUiState.Scanning
            val apiKey = VirusTotalConfig.apiKey.trim()
            if (apiKey.isBlank()) {
                _events.emit("VirusTotal API key missing. Add VIRUSTOTAL_API_KEY to local.properties.")
                _uiState.value = SandboxUiState.Idle
                return@launch
            }

            runCatching {
                withContext(Dispatchers.IO) {
                    val id = Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(url.toByteArray(Charsets.UTF_8))
                    virusTotalApiService.getUrlReport(id = id, apiKey = apiKey)
                }
            }.onSuccess { resp ->
                val attrs = resp.data?.attributes
                val stats = attrs?.lastAnalysisStats
                val malicious = stats?.malicious ?: 0
                val suspicious = stats?.suspicious ?: 0
                val harmless = stats?.harmless ?: 0

                val results = attrs?.lastAnalysisResults.orEmpty()
                val flagged = results.values
                    .asSequence()
                    .filter { it.category == "malicious" || it.category == "suspicious" }
                    .map {
                        val engine = it.engineName ?: "Unknown"
                        val result = it.result ?: it.category ?: "Flagged"
                        "$engine: $result"
                    }
                    .distinct()
                    .sorted()
                    .toList()

                _uiState.value =
                    if (malicious > 0 || suspicious > 0) {
                        SandboxUiState.Scam(
                            url = url,
                            harmless = harmless,
                            malicious = malicious,
                            suspicious = suspicious,
                            flaggedEngines = flagged,
                        )
                    } else {
                        SandboxUiState.Safe(
                            url = url,
                            harmless = harmless,
                            malicious = malicious,
                            suspicious = suspicious,
                        )
                    }
            }.onFailure { e ->
                _events.emit("VirusTotal scan failed: ${e.message ?: "Unknown error"}")
                _uiState.value = SandboxUiState.Idle
            }
        }
    }
}

