package com.aegis.shield.util

import com.aegis.shield.BuildConfig

object TwilioConfig {
    val accountSid: String get() = BuildConfig.TWILIO_ACCOUNT_SID
    val authToken: String get() = BuildConfig.TWILIO_AUTH_TOKEN
    val fromNumber: String get() = BuildConfig.TWILIO_FROM_NUMBER
}
