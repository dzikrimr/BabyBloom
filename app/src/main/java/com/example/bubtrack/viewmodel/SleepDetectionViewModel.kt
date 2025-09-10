package com.example.bubtrack.viewmodel

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.camera.core.ExperimentalGetImage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bubtrack.data.database.SleepSessionEntity
import com.example.bubtrack.data.models.BabyFeatures
import com.example.bubtrack.data.models.SleepStatus
import com.example.bubtrack.repository.SleepRepository
import com.example.bubtrack.utils.NotificationHelper
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import com.google.mlkit.vision.pose.*
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class SleepDetectionViewModel(
    private val repository: SleepRepository,
    private val notificationHelper: NotificationHelper,
    private val context: Context
) : ViewModel() {

    private val _sleepStatus = MutableStateFlow(SleepStatus())
    val sleepStatus: StateFlow<SleepStatus> = _sleepStatus.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private var currentSession: SleepSessionEntity? = null
    private var frameCount = 0
    private var totalEAR = 0f
    private var totalMovement = 0f
    private var rolloverCount = 0
    private var consecutiveFramesWithoutFace = 0
    private var consecutiveFramesWithFace = 0
    private var consecutiveLowMovementFrames = 0

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
    private var previousFaceContours: Map<Int, List<android.graphics.PointF>>? = null
    private var smoothedMovement = 0f
    private val emaAlpha = 0.15f
    private val movementHistory = mutableListOf<Float>()
    private var maxHistorySize = 20
    private val movementThreshold = 0.15f

    init {
        startNewSession()
    }

    private fun startNewSession() {
        currentSession = SleepSessionEntity(startTime = System.currentTimeMillis())
        viewModelScope.launch {
            currentSession?.let { repository.insertSession(it) }
            Log.d("SleepDetection", "New session started")
        }
    }

    @ExperimentalGetImage
    fun processFrame(imageProxy: ImageProxy) {
        if (_isProcessing.value) {
            Log.d("SleepDetection", "Skipping frame: Already processing")
            imageProxy.close()
            return
        }

        _isProcessing.value = true

        viewModelScope.launch {
            try {
                val mediaImage = imageProxy.image
                if (mediaImage == null) {
                    Log.e("SleepDetection", "ImageProxy has no image")
                    _sleepStatus.value = _sleepStatus.value.copy(
                        overallStatus = "Error: No image data"
                    )
                    return@launch
                }

                val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                val features = extractFeatures(inputImage)
                val status = analyzeFeatures(features)
                _sleepStatus.value = status
                updateSession(features)
                Log.d("SleepDetection", "Processed frame: Features=$features, Status=$status")
            } catch (e: Exception) {
                Log.e("SleepDetection", "Error processing frame: ${e.message}", e)
                _sleepStatus.value = _sleepStatus.value.copy(
                    overallStatus = "Error: ${e.message}"
                )
            } finally {
                imageProxy.close()
                _isProcessing.value = false
            }
        }
    }

    private suspend fun extractFeatures(inputImage: InputImage): BabyFeatures =
        withContext(Dispatchers.Default) {
            var features = BabyFeatures()
            var faceDetected = false
            var poseDetected = false
            var pose: Pose? = null

            try {
                val faces = faceDetector.process(inputImage).await()
                Log.d("SleepDetection", "Faces detected: ${faces.size}")
                if (faces.isNotEmpty()) {
                    faceDetected = true
                    consecutiveFramesWithFace++
                    consecutiveFramesWithoutFace = 0
                    val face = faces[0]
                    val faceMovement = calculateFaceMovement(face)

                    features = features.copy(
                        eyeAspectRatio = calculateEAR(face),
                        movement = faceMovement,
                        isRollover = false
                    )
                } else {
                    consecutiveFramesWithoutFace++
                    consecutiveFramesWithFace = 0
                    Log.w("SleepDetection", "No faces detected - consecutive frames without face: $consecutiveFramesWithoutFace")
                }
            } catch (e: Exception) {
                Log.e("SleepDetection", "Face detection error: ${e.message}", e)
            }

            try {
                pose = poseDetector.process(inputImage).await()
                if (pose != null && pose.allPoseLandmarks.isNotEmpty()) {
                    poseDetected = true
                    val limbMovement = calculateLimbMovement(pose.allPoseLandmarks)

                    val combinedMovement = if (faceDetected) {
                        (features.movement * 0.5f + limbMovement * 0.5f) // Adjusted weights
                    } else {
                        limbMovement
                    }

                    features = features.copy(
                        movement = combinedMovement,
                        isRollover = if (!faceDetected) detectRollover(pose.allPoseLandmarks) else features.isRollover
                    )
                    Log.d("SleepDetection", "Pose detected: Landmarks=${pose.allPoseLandmarks.size}, LimbMovement=$limbMovement, Combined=$combinedMovement")
                } else {
                    Log.w("SleepDetection", "No pose detected")
                }
            } catch (e: Exception) {
                Log.e("SleepDetection", "Pose detection error: ${e.message}", e)
            }

            smoothedMovement = emaAlpha * features.movement + (1 - emaAlpha) * smoothedMovement

            movementHistory.add(smoothedMovement)
            if (movementHistory.size > maxHistorySize) {
                movementHistory.removeAt(0)
            }

            val finalMovement = if (smoothedMovement > movementThreshold && poseDetected && (pose?.allPoseLandmarks?.size ?: 0) >= 4 && movementHistory.count { it > movementThreshold } >= 3) {
                smoothedMovement
            } else {
                if (smoothedMovement < movementThreshold) {
                    consecutiveLowMovementFrames++
                    if (consecutiveLowMovementFrames >= maxHistorySize) {
                        previousPoseLandmarks = null
                        Log.d("SleepDetection", "Reset previousPoseLandmarks: Consistently low movement")
                    }
                } else {
                    consecutiveLowMovementFrames = 0
                }
                0f
            }
            features = features.copy(movement = finalMovement)

            if (!faceDetected && consecutiveFramesWithoutFace >= 3) {
                features = features.copy(isRollover = true)
                Log.d("SleepDetection", "Rollover detected: No face for $consecutiveFramesWithoutFace consecutive frames")
            }

            if (!faceDetected && !poseDetected) {
                Log.w("SleepDetection", "No detections: Both face and pose detection failed")
            }

            features
        }

    private fun calculateEAR(face: Face): Float {
        val leftEyeOpen = face.leftEyeOpenProbability ?: 0.5f
        val rightEyeOpen = face.rightEyeOpenProbability ?: 0.5f
        val ear = (leftEyeOpen + rightEyeOpen) / 2f
        Log.d("SleepDetection", "EAR calculated: Left=$leftEyeOpen, Right=$rightEyeOpen, Avg=$ear")
        return ear
    }

    private fun calculateFaceMovement(face: Face): Float {
        val currentContours = mutableMapOf<Int, List<android.graphics.PointF>>()
        val importantContours = listOf(
            FaceContour.FACE,
            FaceContour.NOSE_BRIDGE,
            FaceContour.LEFT_EYE,
            FaceContour.RIGHT_EYE
        )

        importantContours.forEach { contourType ->
            face.getContour(contourType)?.let { contour ->
                currentContours[contourType] = contour.points
            }
        }

        val movement = if (previousFaceContours != null && currentContours.isNotEmpty()) {
            var totalMovement = 0f
            var pointCount = 0

            currentContours.forEach { (contourType, points) ->
                previousFaceContours!![contourType]?.let { prevPoints ->
                    if (points.size == prevPoints.size) {
                        points.forEachIndexed { index, point ->
                            val prevPoint = prevPoints[index]
                            val distance = sqrt(
                                (point.x - prevPoint.x).pow(2) +
                                        (point.y - prevPoint.y).pow(2)
                            )
                            totalMovement += distance
                            pointCount++
                        }
                    }
                }
            }

            if (pointCount > 0) totalMovement / pointCount else 0f
        } else {
            0f
        }

        previousFaceContours = currentContours
        val normalizedMovement = if (movement > 10f) movement / 100f else 0f
        Log.d("SleepDetection", "Face contour movement: Raw=$movement, Normalized=$normalizedMovement")
        return normalizedMovement
    }

    private fun calculateLimbMovement(landmarks: List<PoseLandmark>): Float {
        val limbLandmarks = landmarks.filter {
            it.landmarkType in listOf(
                PoseLandmark.LEFT_KNEE,
                PoseLandmark.RIGHT_KNEE,
                PoseLandmark.LEFT_ANKLE,
                PoseLandmark.RIGHT_ANKLE,
                PoseLandmark.LEFT_WRIST,
                PoseLandmark.RIGHT_WRIST,
                PoseLandmark.LEFT_ELBOW,
                PoseLandmark.RIGHT_ELBOW
            ) && it.inFrameLikelihood > 0.8f
        }

        if (previousPoseLandmarks == null || limbLandmarks.size < 4) {
            previousPoseLandmarks = limbLandmarks
            Log.d("SleepDetection", "No previous or insufficient limb landmarks (${limbLandmarks.size}), movement=0")
            return 0f
        }

        var totalMovement = 0f
        var validComparisons = 0
        var legMovement = 0f
        var armMovement = 0f
        var legComparisons = 0
        var armComparisons = 0

        limbLandmarks.forEach { current ->
            previousPoseLandmarks?.find {
                it.landmarkType == current.landmarkType && it.inFrameLikelihood > 0.8f
            }?.let { previous ->
                val distance = sqrt(
                    (current.position.x - previous.position.x).pow(2) +
                            (current.position.y - previous.position.y).pow(2)
                )
                if (current.landmarkType in listOf(
                        PoseLandmark.LEFT_KNEE, PoseLandmark.RIGHT_KNEE,
                        PoseLandmark.LEFT_ANKLE, PoseLandmark.RIGHT_ANKLE
                    )) {
                    legMovement += distance
                    legComparisons++
                } else {
                    armMovement += distance
                    armComparisons++
                }
                totalMovement += distance
                validComparisons++
            }
        }

        previousPoseLandmarks = limbLandmarks

        val averageLegMovement = if (legComparisons > 0) legMovement / legComparisons else 0f
        val averageArmMovement = if (armComparisons > 0) armMovement / armComparisons else 0f
        val weightedMovement = (averageLegMovement * 0.7f + averageArmMovement * 0.3f)
        val normalizedMovement = if (weightedMovement > 20f) weightedMovement / 200f else 0f

        Log.d("SleepDetection", "Limb movement: Legs=$averageLegMovement, Arms=$averageArmMovement, Weighted=$weightedMovement, Normalized=$normalizedMovement")
        return normalizedMovement
    }

    private fun detectRollover(landmarks: List<PoseLandmark>): Boolean {
        val nose = landmarks.find { it.landmarkType == PoseLandmark.NOSE && it.inFrameLikelihood > 0.7f }
        val leftShoulder = landmarks.find { it.landmarkType == PoseLandmark.LEFT_SHOULDER && it.inFrameLikelihood > 0.5f }
        val rightShoulder = landmarks.find { it.landmarkType == PoseLandmark.RIGHT_SHOULDER && it.inFrameLikelihood > 0.5f }
        val leftHip = landmarks.find { it.landmarkType == PoseLandmark.LEFT_HIP && it.inFrameLikelihood > 0.5f }
        val rightHip = landmarks.find { it.landmarkType == PoseLandmark.RIGHT_HIP && it.inFrameLikelihood > 0.5f }

        if (nose == null || nose.inFrameLikelihood < 0.5f) {
            Log.d("SleepDetection", "Rollover detected: Nose not visible or low confidence")
            return true
        }

        if (leftShoulder != null && rightShoulder != null && leftHip != null && rightHip != null) {
            val shoulderYDiff = abs(leftShoulder.position.y - rightShoulder.position.y)
            val hipYDiff = abs(leftHip.position.y - rightHip.position.y)
            val isRollover = shoulderYDiff > 20f || hipYDiff > 20f
            Log.d("SleepDetection", "Rollover detection from pose: ShoulderYDiff=$shoulderYDiff, HipYDiff=$hipYDiff, Rollover=$isRollover")
            return isRollover
        }

        Log.d("SleepDetection", "Rollover detection: Insufficient pose landmarks")
        return false
    }

    private fun analyzeFeatures(features: BabyFeatures): SleepStatus {
        if (features.eyeAspectRatio == 0f && features.movement == 0f) {
            return SleepStatus(
                eyeStatus = "Mata: Tidak terdeteksi",
                movementStatus = "Pergerakan: Tidak terdeteksi",
                rolloverStatus = if (consecutiveFramesWithoutFace >= 3) "Rollover: Kemungkinan Ya" else "Rollover: Tidak terdeteksi",
                overallStatus = "Tidak ada deteksi: Periksa kamera atau pencahayaan"
            )
        }

        val eyeStatus = when {
            features.eyeAspectRatio > 0.7f -> "Mata terbuka [${String.format("%.2f", features.eyeAspectRatio)}]"
            features.eyeAspectRatio > 0.3f -> "Mata setengah terbuka [${String.format("%.2f", features.eyeAspectRatio)}]"
            else -> "Mata tertutup [${String.format("%.2f", features.eyeAspectRatio)}]"
        }

        val movementStatus = when {
            features.movement > 0.2f -> "Bergerak aktif! [${String.format("%.3f", features.movement)}]"
            features.movement > 0.15f -> "Gerakan ringan [${String.format("%.3f", features.movement)}]"
            else -> "Tidak bergerak [${String.format("%.3f", features.movement)}]"
        }

        val rolloverStatus = when {
            features.isRollover && consecutiveFramesWithoutFace >= 3 -> "Rollover: Ya (wajah tidak terlihat)"
            features.isRollover -> "Rollover: Kemungkinan Ya"
            else -> "Rollover: Tidak"
        }

        val overallStatus = when {
            features.isRollover -> {
                notificationHelper.sendRolloverAlert()
                "⚠️ PERHATIAN: Bayi mungkin rollover!"
            }
            features.movement > 0.2f -> {
                notificationHelper.sendMovementAlert()
                "😴 Bayi aktif/bergerak"
            }
            features.eyeAspectRatio < 0.3f && features.movement == 0f -> "😴 Bayi tampak tidur"
            consecutiveFramesWithoutFace >= 3 -> "⚠️ Wajah tidak terlihat - kemungkinan rollover"
            else -> "👶 Bayi terjaga"
        }

        return SleepStatus(
            eyeStatus = eyeStatus,
            movementStatus = movementStatus,
            rolloverStatus = rolloverStatus,
            overallStatus = overallStatus
        )
    }

    private fun updateSession(features: BabyFeatures) {
        frameCount++
        totalEAR += features.eyeAspectRatio
        totalMovement += features.movement
        if (features.isRollover) rolloverCount++

        currentSession?.let { session ->
            viewModelScope.launch {
                repository.updateSession(
                    session.copy(
                        averageEAR = totalEAR / frameCount,
                        averageMovement = totalMovement / frameCount,
                        rolloverCount = rolloverCount,
                        pacifierUsage = false,
                        totalFramesProcessed = frameCount
                    )
                )
                Log.d("SleepDetection", "Session updated: Frames=$frameCount, AvgEAR=${totalEAR / frameCount}, AvgMovement=${totalMovement / frameCount}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        faceDetector.close()
        poseDetector.close()
        viewModelScope.launch {
            currentSession?.let {
                repository.updateSession(it.copy(endTime = System.currentTimeMillis()))
            }
            Log.d("SleepDetection", "ViewModel cleared, session ended")
        }
    }
}