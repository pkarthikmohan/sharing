package com.aegis.shield.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.shield.domain.SendOtpUseCase
import com.aegis.shield.util.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class SafeWordUiState(
    val generatedPin: String? = null,
    val isSending: Boolean = false,
    val secondsRemaining: Int = 0,
    val errorMessage: String? = null,
    val smsSent: Boolean = false,
)

@HiltViewModel
class SafeWordViewModel @Inject constructor(
    private val sendOtpUseCase: SendOtpUseCase,
) : ViewModel() {
    private var timerJob: Job? = null

    private val _uiState = MutableStateFlow(SafeWordUiState())
    val uiState: StateFlow<SafeWordUiState> = _uiState.asStateFlow()

    fun sendPin(toNumber: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true, errorMessage = null, smsSent = false)
            when (val result = sendOtpUseCase(toNumber)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        generatedPin = result.data,
                        isSending = false,
                        secondsRemaining = 30,
                        smsSent = true,
                        errorMessage = null,
                    )
                    startCountdown()
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        smsSent = false,
                        errorMessage = result.message,
                    )
                }
            }
        }
    }

    fun clearSmsSentFlag() {
        _uiState.value = _uiState.value.copy(smsSent = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (seconds in 30 downTo 0) {
                _uiState.value = _uiState.value.copy(secondsRemaining = seconds)
                delay(1_000)
            }
            _uiState.value = _uiState.value.copy(generatedPin = null, secondsRemaining = 0)
        }
    }
}
