package com.aegis.shield.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TwilioSmsRequest(
    @SerializedName("To") val to: String,
    @SerializedName("From") val from: String,
    @SerializedName("Body") val body: String,
)
