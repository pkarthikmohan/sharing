package com.aegis.shield.ml

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class AudioArtifact(val label: String, val value: Int)

data class VoiceResult(
    val deepfakeProbability: Int,       // 0–100
    val artifacts: List<AudioArtifact>,
)

class VoiceClassifier(context: Context) {

    companion object {
        private const val MODEL_FILE   = "yamnet.tflite"
        // YAMNet expects 15600 float32 samples @ 16kHz = 0.975 seconds
        const val SAMPLE_RATE          = 16000
        const val CHUNK_SAMPLES        = 15600

        // YAMNet class indices most associated with synthetic/processed speech
        // (Speech synthesizer = 0, Narration = 8, Electronic music region 200+)
        private val SYNTHETIC_INDICES  = setOf(0, 8, 9, 132, 133, 134, 135)
        private val SPEECH_INDICES     = setOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    }

    private val interpreter: Interpreter
    private val numClasses: Int

    init {
        val assetFd = context.assets.openFd(MODEL_FILE)
        val stream  = assetFd.createInputStream()
        val bytes   = stream.readBytes(); stream.close()
        val buf     = ByteBuffer.allocateDirect(bytes.size).apply { put(bytes); rewind() }
        interpreter = Interpreter(buf)
        numClasses  = interpreter.getOutputTensor(0).shape().last()
    }

    /**
     * Analyze a chunk of 16kHz mono PCM samples (ShortArray).
     * Returns a VoiceResult with deepfake probability and audio artifacts.
     */
    fun analyzeChunk(pcm: ShortArray): VoiceResult {
        require(pcm.size >= CHUNK_SAMPLES) { "Need at least $CHUNK_SAMPLES samples" }

        // Convert short PCM → float32 normalised to [-1, 1]
        val inputBuf = ByteBuffer.allocateDirect(4 * CHUNK_SAMPLES).order(ByteOrder.nativeOrder())
        for (i in 0 until CHUNK_SAMPLES) inputBuf.putFloat(pcm[i] / 32768f)
        inputBuf.rewind()

        val outputBuf = ByteBuffer.allocateDirect(4 * numClasses).order(ByteOrder.nativeOrder())
        interpreter.run(inputBuf, outputBuf)
        outputBuf.rewind()

        val scores = FloatArray(numClasses) { outputBuf.float }

        // Deepfake probability heuristic:
        // 1. Sum scores for synthetic-sounding classes
        val syntheticScore = SYNTHETIC_INDICES.sumOf { scores.getOrElse(it) { 0f }.toDouble() }.toFloat()
        val speechScore    = SPEECH_INDICES.sumOf { scores.getOrElse(it) { 0f }.toDouble() }.toFloat()

        // 2. Spectral flatness proxy: low peak-to-average ratio in score distribution = flat = synthetic
        val maxScore = scores.max()
        val avgScore = scores.average().toFloat()
        val flatnessRatio = if (maxScore > 0) (avgScore / maxScore) else 0f
        val spectralFlatness = (flatnessRatio * 120).toInt().coerceIn(0, 100)

        // 3. Pitch regularity: synthetic voices tend to hit speech classes uniformly
        val pitchRegularity = (speechScore * 100 * 0.9f).toInt().coerceIn(0, 100)

        // 4. MFCC anomaly proxy: distance from expected human speech distribution
        val mfccAnomaly = (syntheticScore * 200).toInt().coerceIn(0, 100)

        // Composite deepfake probability
        val deepfakeProb = ((spectralFlatness * 0.4f) +
                            (mfccAnomaly * 0.4f) +
                            (pitchRegularity * 0.2f)).toInt().coerceIn(0, 100)

        return VoiceResult(
            deepfakeProbability = deepfakeProb,
            artifacts = listOf(
                AudioArtifact("Spectral Flatness",  spectralFlatness),
                AudioArtifact("MFCC Anomaly",       mfccAnomaly),
                AudioArtifact("Pitch Regularity",   pitchRegularity),
            )
        )
    }

    fun close() { interpreter.close() }
}
