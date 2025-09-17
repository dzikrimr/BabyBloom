package com.example.bubtrack.presentation.ai.monitor

import android.graphics.PointF
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.example.bubtrack.models.SleepFeatures
import com.example.bubtrack.models.SleepStatus
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import com.google.mlkit.vision.pose.*
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow
import kotlin.math.sqrt

@Singleton
class SleepDetectionAnalyzer @Inject constructor() {

    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setMinFaceSize(0.05f)
            .enableTracking()
            .build()
    )

    private val poseDetector = PoseDetection.getClient(
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
    )

    private var previousPoseLandmarks: List<PoseLandmark>? = null
    private var previousFaceContours: Map<Int, List<PointF>>? = null
    private var smoothedMovement = 0f
    private val emaAlpha = 0.15f
    private val movementHistory = mutableListOf<Float>()
    private var consecutiveFramesWithoutFace = 0
    private var consecutiveLowMovementFrames = 0
    private val movementThreshold = 0.15f
    private val maxHistorySize = 20

    @ExperimentalGetImage
    suspend fun analyzeFrame(imageProxy: ImageProxy): SleepStatus {
        val mediaImage = imageProxy.image ?: return SleepStatus(overallStatus = "Error: No image data")
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val features = extractFeatures(inputImage)
        return analyzeFeatures(features)
    }

    private suspend fun extractFeatures(inputImage: InputImage): SleepFeatures {
        var features = SleepFeatures()
        var faceDetected = false
        var poseDetected = false
        var pose: Pose? = null

        try {
            val faces = faceDetector.process(inputImage).await()
            if (faces.isNotEmpty()) {
                faceDetected = true
                consecutiveFramesWithoutFace = 0
                val face = faces[0]
                val faceMovement = calculateFaceMovement(face)
                features = features.copy(
                    eyeAspectRatio = calculateEAR(face),
                    movement = faceMovement,
                )
            } else {
                consecutiveFramesWithoutFace++
            }
        } catch (e: Exception) {
            Log.e("SleepAnalyzer", "Face detection error: ${e.message}")
        }

        try {
            pose = poseDetector.process(inputImage).await()
            if (pose != null && pose.allPoseLandmarks.isNotEmpty()) {
                poseDetected = true
                val limbMovement = calculateLimbMovement(pose.allPoseLandmarks)
                val combinedMovement = if (faceDetected) {
                    (features.movement * 0.5f + limbMovement * 0.5f)
                } else limbMovement

                features = features.copy(
                    movement = combinedMovement,
                    isRollover = if (!faceDetected) detectRollover(pose.allPoseLandmarks) else features.isRollover
                )
            }
        } catch (e: Exception) {
            Log.e("SleepAnalyzer", "Pose detection error: ${e.message}")
        }

        smoothedMovement = emaAlpha * features.movement + (1 - emaAlpha) * smoothedMovement
        movementHistory.add(smoothedMovement)
        if (movementHistory.size > maxHistorySize) {
            movementHistory.removeAt(0)
        }

        if (!faceDetected && consecutiveFramesWithoutFace >= 3) {
            features = features.copy(isRollover = true)
        }

        return features
    }

    private fun calculateEAR(face: Face): Float {
        val left = face.leftEyeOpenProbability ?: 0.5f
        val right = face.rightEyeOpenProbability ?: 0.5f
        return (left + right) / 2f
    }

    private fun calculateFaceMovement(face: Face): Float {
        val currentContours = mutableMapOf<Int, List<PointF>>()
        val important = listOf(FaceContour.FACE, FaceContour.NOSE_BRIDGE, FaceContour.LEFT_EYE, FaceContour.RIGHT_EYE)
        important.forEach { type ->
            face.getContour(type)?.let { currentContours[type] = it.points }
        }

        val movement = if (previousFaceContours != null) {
            var total = 0f
            var count = 0
            currentContours.forEach { (t, points) ->
                previousFaceContours!![t]?.let { prev ->
                    if (points.size == prev.size) {
                        points.forEachIndexed { i, p ->
                            val prevP = prev[i]
                            total += sqrt((p.x - prevP.x).pow(2) + (p.y - prevP.y).pow(2))
                            count++
                        }
                    }
                }
            }
            if (count > 0) total / count else 0f
        } else 0f

        previousFaceContours = currentContours
        return if (movement > 10f) movement / 100f else 0f
    }

    private fun calculateLimbMovement(landmarks: List<PoseLandmark>): Float {
        if (previousPoseLandmarks == null) {
            previousPoseLandmarks = landmarks
            return 0f
        }
        var total = 0f
        var count = 0
        landmarks.forEach { cur ->
            previousPoseLandmarks?.find { it.landmarkType == cur.landmarkType }?.let { prev ->
                total += sqrt((cur.position.x - prev.position.x).pow(2) + (cur.position.y - prev.position.y).pow(2))
                count++
            }
        }
        previousPoseLandmarks = landmarks
        return if (count > 0) total / count else 0f
    }

    private fun detectRollover(landmarks: List<PoseLandmark>): Boolean {
        val nose = landmarks.find { it.landmarkType == PoseLandmark.NOSE && it.inFrameLikelihood > 0.7f }
        return nose == null
    }

    private var lastEyesClosedTime: Long = 0

    private fun analyzeFeatures(features: SleepFeatures): SleepStatus {
        val now = System.currentTimeMillis()

        val eyeStatus = when {
            features.eyeAspectRatio > 0.7f -> "Mata terbuka"
            features.eyeAspectRatio > 0.3f -> "Mata setengah terbuka"
            else -> "Mata tertutup"
        }

        val moveStatus = when {
            features.movement > 0.2f -> "Bergerak aktif"
            features.movement > 0.15f -> "Gerakan ringan"
            else -> "Tidak bergerak"
        }

        val rolloverStatus = if (features.isRollover) "⚠️ Bayi tengkurap" else "Tidak tengkurap"

        val overall = when {
            features.isRollover -> {
                // rollover selalu ditampilkan prioritas
                "⚠️ Wajah bayi mungkin tertutup sesuatu"
            }
            features.eyeAspectRatio < 0.3f -> { // mata tertutup
                if (lastEyesClosedTime == 0L) {
                    lastEyesClosedTime = now
                }
                val durationClosed = now - lastEyesClosedTime

                if (features.movement < 0.15f && durationClosed > 30_000) {
                    "😴 Bayi tidur"
                } else if (features.movement < 0.15f) {
                    "💤 Bayi mengantuk"
                } else {
                    "👶 Bayi bergerak walau mata tertutup"
                }
            }
            else -> {
                lastEyesClosedTime = 0
                "👀 Bayi terjaga"
            }
        }

        return SleepStatus(
            eyeStatus = eyeStatus,
            movementStatus = moveStatus,
            rolloverStatus = rolloverStatus,
            overallStatus = overall
        )
    }
}
