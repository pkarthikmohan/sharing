package com.aegis.shield.data

import android.util.Base64
import com.aegis.shield.data.remote.dto.TwilioSmsRequest
import com.aegis.shield.data.remote.TwilioApiService
import com.aegis.shield.util.AppResult
import com.aegis.shield.util.TwilioConfig
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject

@Singleton
class TwilioRepositoryImpl @Inject constructor(
    private val twilioApiService: TwilioApiService,
) : TwilioRepository {
    override suspend fun sendVerificationSms(toNumber: String, message: String): AppResult<Unit> {
        val sid = TwilioConfig.accountSid
        val token = TwilioConfig.authToken
        val from = TwilioConfig.fromNumber

        if (sid.isBlank() || token.isBlank() || from.isBlank()) {
            return AppResult.Error("Twilio keys missing. Add SID/Auth Token/From Number in BuildConfig.")
        }
        val normalizedTo = toNumber.trim().replace(" ", "")
        if (!normalizedTo.startsWith("+") || normalizedTo.drop(1).any { !it.isDigit() }) {
            return AppResult.Error("Invalid number format. Use E.164 format (e.g. +9198XXXXXX).")
        }

        return try {
            val credential = "$sid:$token"
            val encoded = Base64.encodeToString(credential.toByteArray(), Base64.NO_WRAP)
            val request = TwilioSmsRequest(
                to = normalizedTo,
                from = from,
                body = message,
            )
            val response = twilioApiService.sendSms(
                authorization = "Basic $encoded",
                accountSid = sid,
                to = request.to,
                from = request.from,
                body = request.body,
            )
            if (response.isSuccessful) {
                AppResult.Success(Unit)
            } else {
                val twilioMessage = response.errorBody()?.string()
                    ?.let(::extractTwilioErrorMessage)
                if (response.code() in 400..499) {
                    AppResult.Error(twilioMessage ?: "Invalid number or Twilio request rejected.")
                } else {
                    AppResult.Error("Twilio service unavailable. Try again.")
                }
            }
        } catch (_: IOException) {
            AppResult.Error("No internet connection. Please retry.")
        } catch (_: HttpException) {
            AppResult.Error("Twilio API error. Please try again.")
        }
    }

    private fun extractTwilioErrorMessage(errorBody: String): String? {
        return runCatching {
            val json = JSONObject(errorBody)
            json.optString("message").takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}
