package com.aegis.shield.domain

import com.aegis.shield.data.TwilioRepository
import com.aegis.shield.data.TwilioSessionState
import com.aegis.shield.util.AppResult
import com.aegis.shield.util.generateSixDigitPin
import javax.inject.Inject

class SendOtpUseCase @Inject constructor(
    private val twilioRepository: TwilioRepository,
    private val twilioSessionState: TwilioSessionState,
) {
    suspend operator fun invoke(toNumber: String): AppResult<String> {
        val pin = generateSixDigitPin()
        twilioSessionState.savePin(pin)
        val message = "Aegis verification code: $pin. Valid for 30 seconds."
        return when (val result = twilioRepository.sendVerificationSms(toNumber, message)) {
            is AppResult.Success -> AppResult.Success(pin)
            is AppResult.Error -> result
        }
    }
}
