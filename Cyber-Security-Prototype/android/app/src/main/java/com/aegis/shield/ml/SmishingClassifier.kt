package com.aegis.shield.ml

import android.content.Context
import com.aegis.shield.data.ThreatBand
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class ThreatVector(val name: String, val score: Int, val icon: String)

data class SmishingResult(
    val score: Int,           // 0–100
    val band: ThreatBand,
    val vectors: List<ThreatVector>,
    val explanation: String,
)

class SmishingClassifier(context: Context) {

    companion object {
        private const val MODEL_FILE = "sms_model.tflite"
        private const val VOCAB_FILE = "vocab.txt"
        private const val PAD_ID     = 0
        private const val OOV_ID     = 1
    }

    private val interpreter: Interpreter
    private val vocab: Map<String, Int>
    private val seqLen: Int

    init {
        // Load model from assets
        val assetFd = context.assets.openFd(MODEL_FILE)
        val fileInput = assetFd.createInputStream()
        val modelBytes = fileInput.readBytes()
        fileInput.close()
        val modelBuffer = ByteBuffer.allocateDirect(modelBytes.size)
            .apply { put(modelBytes); rewind() }

        interpreter = Interpreter(modelBuffer)

        // Detect sequence length from model input tensor shape [1, seqLen]
        val shape = interpreter.getInputTensor(0).shape()
        seqLen = if (shape.size >= 2) shape[1] else 150

        // Load vocabulary: line number = token index
        vocab = buildMap {
            BufferedReader(InputStreamReader(context.assets.open(VOCAB_FILE))).useLines { lines ->
                lines.forEachIndexed { idx, word -> put(word.trim().lowercase(), idx) }
            }
        }
    }

    fun classify(smsText: String): SmishingResult {
        // 1. Tokenize
        val tokens = smsText.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .map { vocab[it] ?: OOV_ID }

        // 2. Pad / truncate to seqLen
        val padded = IntArray(seqLen) { PAD_ID }
        tokens.take(seqLen).forEachIndexed { i, v -> padded[i] = v }

        // 3. Build input ByteBuffer (int32)
        val inputBuf = ByteBuffer.allocateDirect(4 * seqLen)
            .order(ByteOrder.nativeOrder())
        padded.forEach { inputBuf.putInt(it) }
        inputBuf.rewind()

        // 4. Run inference — auto-detect output shape
        val outShape = interpreter.getOutputTensor(0).shape()
        val outputSize = outShape.last()
        val outputBuf = ByteBuffer.allocateDirect(4 * outputSize).order(ByteOrder.nativeOrder())
        interpreter.run(inputBuf, outputBuf)
        outputBuf.rewind()

        // 5. Parse scam probability
        val scamProb: Float = if (outputSize == 1) {
            outputBuf.float
        } else {
            // [safe_prob, scam_prob]
            outputBuf.float   // discard safe
            outputBuf.float
        }

        val score = (scamProb * 100).toInt().coerceIn(0, 100)

        // 6. Band + heuristic threat vectors
        val band = when {
            score >= 85 -> ThreatBand.CONFIRMED
            score >= 65 -> ThreatBand.LIKELY
            score >= 40 -> ThreatBand.SUSPICIOUS
            else        -> ThreatBand.SAFE
        }

        val vectors = buildThreatVectors(smsText, score)
        val explanation = buildExplanation(smsText, band)

        return SmishingResult(score, band, vectors, explanation)
    }

    private fun buildThreatVectors(text: String, baseScore: Int): List<ThreatVector> {
        val lower = text.lowercase()
        fun heuristic(keywords: List<String>): Int {
            val hits = keywords.count { lower.contains(it) }
            return (baseScore + hits * 5).coerceIn(0, 100)
        }
        return listOf(
            ThreatVector("Urgency",           heuristic(listOf("immediately","urgent","now","expire","last chance")),     "⚡"),
            ThreatVector("Financial Demand",  heuristic(listOf("pay","rs.","₹","fine","amount","bank","upi","challan")),  "💰"),
            ThreatVector("Authority Spoof",   heuristic(listOf("govt","trai","police","income tax","ministry","rbi")),    "🏛️"),
            ThreatVector("Link Lure",         heuristic(listOf("http","click","verify","login","bit.ly",".xyz",".tk")),   "🔗"),
            ThreatVector("Impersonation",     heuristic(listOf("dear customer","your account","your vehicle","notice")),  "👤"),
        )
    }

    private fun buildExplanation(text: String, band: ThreatBand): String = when (band) {
        ThreatBand.CONFIRMED  -> "This message has multiple hallmarks of a scam: urgent financial demand, authority impersonation, and suspicious link patterns. Do not click any links or pay."
        ThreatBand.LIKELY     -> "This message shows several suspicious signals. Treat with caution and do not share personal details."
        ThreatBand.SUSPICIOUS -> "This message has some unusual patterns. Verify the sender through official channels before acting."
        ThreatBand.SAFE       -> "No significant threat signals detected."
    }

    fun close() { interpreter.close() }
}
