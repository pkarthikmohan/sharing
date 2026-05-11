package com.aegis.shield.presentation.screens.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.shield.domain.GetMessagesUseCase
import com.aegis.shield.domain.model.SmsMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class MessagesUiState(
    val isLoading: Boolean = true,
    val messages: List<SmsMessage> = emptyList(),
    val errorMessage: String? = null,
    val hasSmsPermission: Boolean = false,
)

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val getMessagesUseCase: GetMessagesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    private var smsJob: Job? = null

    fun updatePermissionState(granted: Boolean) {
        if (_uiState.value.hasSmsPermission == granted) return
        _uiState.value = _uiState.value.copy(hasSmsPermission = granted)
        if (granted) {
            observeInbox()
        } else {
            smsJob?.cancel()
            _uiState.value = _uiState.value.copy(isLoading = false, messages = emptyList())
        }
    }

    private fun observeInbox() {
        smsJob?.cancel()
        smsJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            getMessagesUseCase()
                .catch { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Unable to read inbox",
                    )
                }
                .collect { list ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        messages = list,
                        errorMessage = null,
                    )
                }
        }
    }
}
