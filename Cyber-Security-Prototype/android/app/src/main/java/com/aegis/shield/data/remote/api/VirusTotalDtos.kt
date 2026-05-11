package com.aegis.shield.data.remote.api

import com.google.gson.annotations.SerializedName

data class VirusTotalUrlReportResponse(
    @SerializedName("data") val data: VirusTotalUrlData? = null,
)

data class VirusTotalUrlData(
    @SerializedName("attributes") val attributes: VirusTotalUrlAttributes? = null,
)

data class VirusTotalUrlAttributes(
    @SerializedName("last_analysis_stats") val lastAnalysisStats: VirusTotalLastAnalysisStats? = null,
    @SerializedName("last_analysis_results") val lastAnalysisResults: Map<String, VirusTotalEngineResult>? = null,
)

data class VirusTotalLastAnalysisStats(
    @SerializedName("malicious") val malicious: Int? = null,
    @SerializedName("suspicious") val suspicious: Int? = null,
    @SerializedName("harmless") val harmless: Int? = null,
    @SerializedName("undetected") val undetected: Int? = null,
    @SerializedName("timeout") val timeout: Int? = null,
)

data class VirusTotalEngineResult(
    @SerializedName("engine_name") val engineName: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("result") val result: String? = null,
    @SerializedName("method") val method: String? = null,
    @SerializedName("engine_version") val engineVersion: String? = null,
)

