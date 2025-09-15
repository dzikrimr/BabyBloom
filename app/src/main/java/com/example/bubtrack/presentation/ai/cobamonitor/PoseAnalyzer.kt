package com.example.bubtrack.presentation.ai.cobamonitor


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PoseAnalyzer(
    context: Context,
    private val listener: PoseAnalysisListener
) {

    interface PoseAnalysisListener {
        fun onPoseAnalyzed(result: PoseAnalysisResult)
    }

    private val TAG = "PoseAnalyzer"

    private val detector: PoseDetector by lazy {
        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
            .build()
        PoseDetection.getClient(options)
    }

    fun analyzeFrame(bitmap: Bitmap) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            detector.process(image)
                .addOnSuccessListener { pose ->
                    val result = mapPoseToResult(pose)
                    listener.onPoseAnalyzed(result)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Pose analysis failed: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "PoseAnalyzer exception: ${e.message}")
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun mapPoseToResult(pose: Pose): PoseAnalysisResult {
        // --- Prone detection ---
        val leftShoulder = pose.getPoseLandmark(com.google.mlkit.vision.pose.PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(com.google.mlkit.vision.pose.PoseLandmark.RIGHT_SHOULDER)

        val isProne = leftShoulder != null && rightShoulder != null &&
                kotlin.math.abs(leftShoulder.position.y - rightShoulder.position.y) < 50

        // --- Sleep detection ---
        val nose = pose.getPoseLandmark(com.google.mlkit.vision.pose.PoseLandmark.NOSE)
        val leftEye = pose.getPoseLandmark(com.google.mlkit.vision.pose.PoseLandmark.LEFT_EYE)
        val rightEye = pose.getPoseLandmark(com.google.mlkit.vision.pose.PoseLandmark.RIGHT_EYE)

        var isSleeping = false
        if (nose != null && leftEye != null && rightEye != null && leftShoulder != null && rightShoulder != null) {
            val avgShoulderY = (leftShoulder.position.y + rightShoulder.position.y) / 2
            val avgEyeY = (leftEye.position.y + rightEye.position.y) / 2

            // Heuristik: kalau hidung & mata sejajar dengan bahu (posisi mendatar)
            if (kotlin.math.abs(avgEyeY - avgShoulderY) < 40 &&
                kotlin.math.abs(nose.position.y - avgShoulderY) < 50
            ) {
                isSleeping = true
            }
        }

        return PoseAnalysisResult(
            proneDetected = isProne,
            isSleeping = isSleeping,
            rawPose = pose
        )
    }

    fun stop() {
        detector.close()
    }
}

data class PoseAnalysisResult(
    val proneDetected: Boolean,
    val isSleeping: Boolean,
    val rawPose: Pose
)
