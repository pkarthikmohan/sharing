package com.aegis.shield.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface VirusTotalApiService {
    @GET("api/v3/urls/{id}")
    suspend fun getUrlReport(
        @Path("id") id: String,
        @Header("x-apikey") apiKey: String,
    ): VirusTotalUrlReportResponse
}

