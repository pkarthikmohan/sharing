package com.aegis.shield.data
import com.aegis.shield.data.local.SmsDataSource
import com.aegis.shield.util.RiskScoreCalculator
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ViewModelScoped
class ThreatSyncManager @Inject constructor(
    private val smsDataSource: SmsDataSource,
    private val threatDao: ThreatDao,
) {
    suspend fun syncInitialThreats(limit: Int = 50) {
        withContext(Dispatchers.IO) {
            val sms = smsDataSource.fetchSmsHistory().take(limit)
            sms.forEach { msg ->
                val analysis = RiskScoreCalculator.analyze(
                    sender = msg.sender,
                    body = msg.body.orEmpty(),
                )
                val score = analysis.riskScore
                if (score < 40) return@forEach

                val band = analysis.band
                threatDao.insert(
                    ThreatEntity(
                        type = ThreatType.SMISHING.name,
                        sender = msg.sender,
                        body = msg.body,
                        score = score,
                        band = band.name,
                        timestamp = msg.timestamp,
                        blocked = band == ThreatBand.CONFIRMED || band == ThreatBand.LIKELY,
                    )
                )
            }
        }
    }
}

