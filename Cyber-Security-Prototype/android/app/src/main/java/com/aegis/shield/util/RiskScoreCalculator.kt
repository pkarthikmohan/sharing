package com.aegis.shield.util

import com.aegis.shield.data.ThreatBand
import kotlin.math.roundToInt

data class ThreatVectorScore(
    val label: String,
    val emoji: String,
    val pct: Int,
)

data class RiskAnalysisResult(
    val riskScore: Int,
    val band: ThreatBand,
    val threatVectors: List<ThreatVectorScore>,
    val explanation: String,
)

object RiskScoreCalculator {

    fun analyze(sender: String, body: String): RiskAnalysisResult {
        val senderLower = sender.trim().lowercase()
        val bodyLower = body.trim().lowercase()
        val combinedText = "$senderLower $bodyLower"

        val urgency = scoreUrgency(combinedText)
        val financial = scoreFinancial(combinedText)
        val authority = scoreAuthority(combinedText)
        val link = scoreLinkLure(combinedText)
        val impersonation = scoreImpersonation(combinedText)

        val vectors = listOf(
            ThreatVectorScore("Urgency", "⚡", urgency),
            ThreatVectorScore("Financial Demand", "💰", financial),
            ThreatVectorScore("Authority Spoof", "🏛️", authority),
            ThreatVectorScore("Link Lure", "🔗", link),
            ThreatVectorScore("Impersonation", "👤", impersonation),
        )

        // Critical: URLs / severe coercion keywords
        val critical =
            containsRiskyTld(bodyLower) ||
                containsEChallan(combinedText) ||
                containsSevereEnforcement(combinedText)

        if (critical) {
            val criticalScore = 88.coerceAtLeast(vectors.maxOf { it.pct }.coerceIn(0, 100))
                .coerceAtMost(95)
            return RiskAnalysisResult(
                riskScore = criticalScore,
                band = ThreatBand.CONFIRMED,
                threatVectors = vectors,
                explanation = buildExplanation(combinedText, ThreatBand.CONFIRMED, vectors),
            )
        }

        // Medium (Suspicious) candidate: promo/transactional headers + promo keywords
        val promoHeader = Regex("(rrlacc|snapdl|amazon|flipkart)", RegexOption.IGNORE_CASE)
            .containsMatchIn(senderLower)
        val promoBody = Regex("(offer|discount|promo|sale|cashback|deal|coupon)", RegexOption.IGNORE_CASE)
            .containsMatchIn(bodyLower)

        val urgencyHits = countOccurrences(bodyLower, listOf("urgent", "hurry", "expire"))
        val financialHits = countOccurrences(
            bodyLower,
            listOf("click", "link", "loan", "kyc", "offer", "discount"),
        )
        val hasShortLink = containsShortLink(bodyLower)

        val hasAnyPromoSignal = promoHeader || promoBody || hasShortLink

        // Dynamic medium score strictly between 40 and 79.
        val mediumScore: Int? = if (hasAnyPromoSignal) {
            val base = 45
            val urgencyBonus = urgencyHits * 5
            val financialBonus = financialHits * 8
            val shortLinkBonus = if (hasShortLink) 10 else 0
            val computed = base + urgencyBonus + financialBonus + shortLinkBonus
            computed.coerceIn(41, 79)
        } else {
            null
        }

        return if (mediumScore != null) {
            RiskAnalysisResult(
                riskScore = mediumScore,
                band = ThreatBand.SUSPICIOUS,
                threatVectors = vectors,
                explanation = buildExplanation(combinedText, ThreatBand.SUSPICIOUS, vectors),
            )
        } else {
            // SAFE: anything else under the medium threshold
            val avg = vectors.map { it.pct }.average().roundToInt()
            val safeScore = avg.coerceIn(0, 39)
            RiskAnalysisResult(
                riskScore = safeScore,
                band = ThreatBand.SAFE,
                threatVectors = vectors,
                explanation = buildExplanation(combinedText, ThreatBand.SAFE, vectors),
            )
        }
    }

    private fun containsRiskyTld(t: String): Boolean =
        Regex("""\.(xyz|tk)(/|$)""", RegexOption.IGNORE_CASE).containsMatchIn(t) ||
            t.contains(".xyz", ignoreCase = true) ||
            t.contains(".tk", ignoreCase = true)

    private fun containsEChallan(text: String): Boolean =
        Regex("""e[\s-]*challan|echallan""", RegexOption.IGNORE_CASE).containsMatchIn(text)

    private fun containsSevereEnforcement(text: String): Boolean =
        listOf(
            "suspend",
            "blocked",
            "block",
            "account suspended",
            "legal action",
        ).any { it in text }

    private fun countOccurrences(text: String, words: List<String>): Int =
        words.sumOf { w ->
            Regex("""\b${Regex.escape(w)}\b""", RegexOption.IGNORE_CASE).findAll(text).count()
        }

    private fun containsShortLink(text: String): Boolean =
        listOf("bit.ly", "t.co", "tinyurl", "short.link").any { text.contains(it, ignoreCase = true) }

    private fun scoreUrgency(t: String): Int {
        var s = 12
        if (Regex("\\b(urgent|immediately|right\\s*now|expire[sd]?|last\\s*chance|act\\s*now|hurry|within\\s*\\d+)\\b").containsMatchIn(t)) s += 42
        if (Regex("\\b(suspend|blocked|blocked\\s*account|legal\\s*action|arrest|penalty)\\b").containsMatchIn(t)) s += 28
        return s.coerceIn(0, 100)
    }

    private fun scoreFinancial(t: String): Int {
        var s = 10
        if (Regex("[₹]|\\brs\\.?\\b|\\brupees?\\b|\\bpay(ment)?\\b|\\bupi\\b|\\botp\\b").containsMatchIn(t)) s += 38
        if (Regex("\\b(lottery|prize|winner|cashback|refund|invoice|due\\s*amount|unpaid)\\b").containsMatchIn(t)) s += 30
        return s.coerceIn(0, 100)
    }

    private fun scoreAuthority(t: String): Int {
        var s = 8
        if (Regex("\\b(govt|government|ministry|police|rbi|uidai|trai|income\\s*tax|cyber\\s*cell)\\b").containsMatchIn(t)) s += 40
        if (Regex("e[\\s-]*challan|echallan", RegexOption.IGNORE_CASE).containsMatchIn(t)) s += 35
        return s.coerceIn(0, 100)
    }

    private fun scoreLinkLure(t: String): Int {
        var s = 5
        if (Regex("https?://|www\\.").containsMatchIn(t)) s += 45
        if (t.contains(".xyz") || Regex("\\b(bit\\.ly|tinyurl|short\\.link)\\b").containsMatchIn(t)) s += 35
        return s.coerceIn(0, 100)
    }

    private fun scoreImpersonation(t: String): Int {
        var s = 8
        if (Regex("\\b(dear\\s+(customer|user)|verify\\s+your|your\\s+account|we\\s+have\\s+detected|kindly\\s+update)\\b").containsMatchIn(t)) s += 38
        if (Regex("\\b(your\\s+vehicle|kyc\\s+update|pan\\s+linked)\\b").containsMatchIn(t)) s += 25
        return s.coerceIn(0, 100)
    }

    private fun buildExplanation(
        text: String,
        band: ThreatBand,
        vectors: List<ThreatVectorScore>,
    ): String {
        val top = vectors.maxByOrNull { it.pct }
        val bits = mutableListOf<String>()
        if (top != null && top.pct >= 55) {
            bits += "Strong ${top.label.lowercase()} signals (${top.pct}%)."
        }
        if (text.contains(".xyz")) bits += "The message references a non-governmental .xyz domain often used in phishing."
        if (Regex("e[\\s-]*challan|echallan", RegexOption.IGNORE_CASE).containsMatchIn(text)) {
            bits += "E-challan lures often combine authority language with a payment link."
        }
        if (Regex("rrlacc|snapdl", RegexOption.IGNORE_CASE).containsMatchIn(text)) {
            bits += "Known scam tokens (RRLACC / SNAPDL) match bulk-smishing fingerprints."
        }
        if (bits.isEmpty()) {
            return when (band) {
                ThreatBand.CONFIRMED ->
                    "Multiple high-risk cues cluster together typical of curated smishing payloads."
                ThreatBand.LIKELY ->
                    "Several moderate indicators suggest this SMS is risky; pause before tapping links."
                ThreatBand.SUSPICIOUS ->
                    "Some cues look off; verify through an official channel you choose yourself."
                ThreatBand.SAFE ->
                    "No strong scam patterns detected in this snippet."
            }
        }
        return bits.joinToString(" ")
    }
}
