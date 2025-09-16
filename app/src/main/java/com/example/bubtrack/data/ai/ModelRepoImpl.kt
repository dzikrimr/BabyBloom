package com.example.bubtrack.data.ai

import android.content.Context
import com.example.bubtrack.domain.ai.audio.MFCCExtractor
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.exp

interface ModelRepository {
    suspend fun loadModel()
    suspend fun classifyAudio(audioData: ShortArray): Pair<String, List<Pair<String, Float>>>
    fun release()
}

class ModelRepoImpl(
    private val context: Context,
    private val mfccExtractor: MFCCExtractor
) : ModelRepository {

    private var interpreter: Interpreter? = null

    override suspend fun loadModel() {
        try {
            val assetManager = context.assets
            val fd = assetManager.openFd("baby_cry_model.tflite")
            FileInputStream(fd.fileDescriptor).use { fis ->
                val channel = fis.channel
                val mapped = channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    fd.startOffset,
                    fd.declaredLength
                )
                interpreter = Interpreter(mapped)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            interpreter = null
            throw e
        }
    }

    override suspend fun classifyAudio(audioData: ShortArray): Pair<String, List<Pair<String, Float>>> {
        val interpreter = interpreter ?: throw IllegalStateException("Model not loaded")

        // Extract MFCC features
        val mfcc = mfccExtractor.extract(audioData)

        // Get input tensor info
        val inputTensor = interpreter.getInputTensor(0)
        val inputShape = inputTensor.shape()
        println("Input shape: ${inputShape.contentToString()}")

        // Prepare input buffer with correct size
        val expectedInputSize = inputShape.fold(1) { acc, dim -> acc * dim }
        val inputBuffer = ByteBuffer.allocateDirect(expectedInputSize * 4)
            .order(ByteOrder.nativeOrder())

        // Handle different input shapes
        when (inputShape.size) {
            2 -> {
                // Shape: [batch_size, features]
                val featuresNeeded = inputShape[1]
                repeat(featuresNeeded) { i ->
                    val value = if (i < mfcc.size) mfcc[i] else 0f
                    inputBuffer.putFloat(value)
                }
            }
            3 -> {
                // Shape: [batch_size, time_steps, features] - for Conv1D
                val timeSteps = inputShape[1]
                val features = inputShape[2]

                if (features == 1) {
                    // Conv1D expects [batch, mfcc_length, 1]
                    repeat(timeSteps) { t ->
                        val value = if (t < mfcc.size) mfcc[t] else 0f
                        inputBuffer.putFloat(value)
                    }
                } else {
                    // Other 3D formats
                    repeat(timeSteps * features) { i ->
                        val value = if (i < mfcc.size) mfcc[i % mfcc.size] else 0f
                        inputBuffer.putFloat(value)
                    }
                }
            }
            else -> {
                // Fallback: flatten and pad/truncate
                repeat(expectedInputSize) { i ->
                    val value = if (i < mfcc.size) mfcc[i] else 0f
                    inputBuffer.putFloat(value)
                }
            }
        }

        inputBuffer.rewind()

        // Get output tensor info
        val outputTensor = interpreter.getOutputTensor(0)
        val outputShape = outputTensor.shape()
        println("Output shape: ${outputShape.contentToString()}")

        val numClasses = if (outputShape.size >= 2) outputShape[1] else outputShape.last()
        val output = Array(1) { FloatArray(numClasses) }

        // Run inference
        try {
            interpreter.run(inputBuffer, output)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Inference failed: ${e.message}", e)
        }

        // Apply softmax to get probabilities
        val rawScores = output[0]
        val scores = softmax(rawScores)

        val labels = listOf(
            "belly_pain", "burping", "cold_hot",
            "discomfort", "hungry", "scared", "tired"
        )

        val results = scores.mapIndexed { idx, score ->
            val label = labels.getOrNull(idx) ?: "Other"
            Pair(label, score)
        }.sortedByDescending { it.second }

        return Pair(results.first().first, results)
    }

    private fun softmax(input: FloatArray): FloatArray {
        val max = input.maxOrNull() ?: 0f
        val exps = input.map { exp((it - max).toDouble()).toFloat() }
        val sum = exps.sum()
        return exps.map { it / sum }.toFloatArray()
    }

    override fun release() {
        interpreter?.close()
        interpreter = null
    }
}