package com.aegis.shield.ui.voice

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LiveCallStateStore {
    private val _state = MutableStateFlow<CallAnalysisState>(CallAnalysisState.Idle)
    val state: StateFlow<CallAnalysisState> = _state.asStateFlow()

    fun update(state: CallAnalysisState) {
        _state.value = state
    }
}
