package com.aegis.shield.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TwilioSessionState @Inject constructor() {
    private val _currentPin = MutableStateFlow<String?>(null)
    val currentPin: StateFlow<String?> = _currentPin.asStateFlow()

    fun savePin(pin: String) {
        _currentPin.value = pin
    }
}
