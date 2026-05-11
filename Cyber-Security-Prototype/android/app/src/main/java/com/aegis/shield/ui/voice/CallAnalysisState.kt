package com.aegis.shield.ui.voice

sealed interface CallAnalysisState {
    data object Idle : CallAnalysisState

    data class SafeCall(
        val phoneNumber: String,
        val callDurationSeconds: Long,
    ) : CallAnalysisState

    data class ScamCall(
        val phoneNumber: String,
        val overallProbability: Float,
        val artifacts: Artifacts,
    ) : CallAnalysisState
}

data class Artifacts(
    val spectralFlatness: Float,
    val mfccAnomaly: Float,
    val pitchRegularity: Float,
)
