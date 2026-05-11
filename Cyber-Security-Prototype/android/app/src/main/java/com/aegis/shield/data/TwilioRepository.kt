package com.aegis.shield.data

import com.aegis.shield.util.AppResult

interface TwilioRepository {
    suspend fun sendVerificationSms(toNumber: String, message: String): AppResult<Unit>
}
