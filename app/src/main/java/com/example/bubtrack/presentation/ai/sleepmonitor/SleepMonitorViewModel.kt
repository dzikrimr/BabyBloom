package com.example.bubtrack.presentation.ai.sleepmonitor

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.camera.core.ExperimentalGetImage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bubtrack.models.SleepFeatures
import com.example.bubtrack.models.SleepStatus
import com.example.bubtrack.data.livekit.LiveKitConnectionState
import com.example.bubtrack.data.livekit.LiveKitService
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import com.google.mlkit.vision.pose.*
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

@HiltViewModel
class SleepMonitorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val liveKitService: LiveKitService
) : ViewModel() {

    private val _sleepStatus = MutableStateFlow(SleepStatus())
    val sleepStatus: StateFlow<SleepStatus> = _sleepStatus.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // LiveKit related state flows
    val connectionState = liveKitService.connectionState
    val isHost = liveKitService.isHost
    val participantCount = liveKitService.participantCount
    val remoteBabyData = liveKitService.remoteBabyData

    private var frameCount = 0
    private var totalEAR = 0f
    private var totalMovement = 0f
    private var rolloverCount = 0
    private var consecutiveFramesWithoutFace = 0
    private var consecutiveFramesWithFace = 0
    private var consecutiveLowMovementFrames = 0
    private var roomInfoGenerated: String? = null

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

    /**
     * Initialize as host when the screen opens
     * This generates room info and connects to LiveKit immediately
     */
    fun initializeAsHost() {
        viewModelScope.launch {
            try {
                if (roomInfoGenerated == null) {
                    // Generate room info and connect immediately as host
                    roomInfoGenerated = liveKitService.generateRoomInfo()
                    val success = liveKitService.startAsHostWithRoomInfo(roomInfoGenerated!!)

                    if (success) {
                        Log.d("SleepMonitorViewModel", "Successfully initialized as host and connected to LiveKit")
                    } else {
                        Log.e("SleepMonitorViewModel", "Failed to connect as host")
                    }
                }
            } catch (e: Exception) {
                Log.e("SleepMonitorViewModel", "Error initializing as host: ${e.message}", e)
            }
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
                    _sleepStatus.value = SleepStatus(
                        eyeStatus = "Mata: Tidak terdeteksi",
                        movementStatus = "Pergerakan: Tidak terdeteksi",
                        rolloverStatus = "Rollover: Tidak terdeteksi"
                    )
                    return@launch
                }

                val inputImage =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                val features = extractFeatures(inputImage)
                val status = analyzeFeatures(features)
                _sleepStatus.value = status
                updateAnalytics(features)

                // Send data to connected devices via LiveKit (only if host and connected)
                if (liveKitService.isHost.value &&
                    liveKitService.connectionState.value == LiveKitConnectionState.CONNECTED) {
                    liveKitService.sendBabyMonitorData(status)
                }

                Log.d("SleepDetection", "Processed frame: Features=$features, Status=$status")
            } catch (e: Exception) {
                Log.e("SleepDetection", "Error processing frame: ${e.message}", e)
                _sleepStatus.value = SleepStatus(
                    eyeStatus = "Mata: Tidak terdeteksi",
                    movementStatus = "Pergerakan: Tidak terdeteksi",
                    rolloverStatus = "Rollover: Tidak terdeteksi"
                )
            } finally {
                imageProxy.close()
                _isProcessing.value = false
            }
        }
    }

    /**
     * Generate room information without connecting to LiveKit
     * Used for QR code generation
     */
    fun generateRoomInfo(): String {
        return roomInfoGenerated ?: liveKitService.generateRoomInfo().also {
            roomInfoGenerated = it
        }
    }

    /**
     * Start as host with pre-generated room info (if not already connected)
     * This is called when showing QR code to actually connect to LiveKit
     */
    fun startAsHostWithRoomInfo(roomInfo: String): Boolean {
        return if (connectionState.value == LiveKitConnectionState.DISCONNECTED) {
            liveKitService.startAsHostWithRoomInfo(roomInfo)
        } else {
            true // Already connected
        }
    }

    /**
     * Start as host - generates room info and connects
     * This is for backward compatibility
     */
    fun startAsHost(): String {
        return roomInfoGenerated ?: run {
            val info = liveKitService.generateRoomInfo()
            roomInfoGenerated = info
            // Connect immediately if not already connected
            if (connectionState.value == LiveKitConnectionState.DISCONNECTED) {
                liveKitService.startAsHostWithRoomInfo(info)
            }
            info
        }
    }

    /**
     * Join as viewer by scanning QR code
     */
    fun joinAsViewer(qrData: String): Boolean {
        return liveKitService.joinAsViewer(qrData)
    }

    /**
     * Disconnect from LiveKit room
     */
    fun disconnectFromRoom() {
        liveKitService.disconnect()
        roomInfoGenerated = null // Reset room info for next session
    }

    fun getLocalVideoTrack() = liveKitService.getLocalVideoTrack()
    fun getRemoteVideoTrack() = liveKitService.remoteVideoTrack

    fun switchCamera() = liveKitService.switchCamera()
    fun toggleVideo() = liveKitService.toggleVideo()
    fun toggleAudio() = liveKitService.toggleAudio()

    private suspend fun extractFeatures(inputImage: InputImage): SleepFeatures =
        withContext(Dispatchers.Default) {
            var features = SleepFeatures()
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
                    Log.w(
                        "SleepDetection",
                        "No faces detected - consecutive frames without face: $consecutiveFramesWithoutFace"
                    )
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
                        (features.movement * 0.3f + limbMovement * 0.7f)
                    } else {
                        limbMovement
                    }

                    features = features.copy(
                        movement = combinedMovement,
                        isRollover = if (!faceDetected) detectRollover(pose.allPoseLandmarks) else features.isRollover
                    )

                    Log.d(
                        "SleepDetection",
                        "Pose detected: Landmarks=${pose.allPoseLandmarks.size}, LimbMovement=$limbMovement, Combined=$combinedMovement"
                    )
                } else {
                    Log.w("SleepDetection", "No pose detected")
                }
            } catch (e: Exception) {
                Log.e("SleepDetection", "Pose detection error: ${e.message}", e)
            }

            smoothedMovement = emaAlpha * features.movement + (1 - emaAlpha) * smoothedMovement
            Log.d("SleepDetection", "Smoothed movement: $smoothedMovement")

            movementHistory.add(smoothedMovement)
            if (movementHistory.size > maxHistorySize) {
                movementHistory.removeAt(0)
            }

            Log.d("SleepDetection", "Movement history: $movementHistory")

            val avgMovementHistory =
                if (movementHistory.isNotEmpty()) movementHistory.average().toFloat() else 0f
            val finalMovement =
                if (smoothedMovement > movementThreshold && poseDetected && (pose?.allPoseLandmarks?.size
                        ?: 0) >= 3 && movementHistory.count { it > movementThreshold } >= 5 && avgMovementHistory > movementThreshold
                ) {
                    smoothedMovement
                } else {
                    if (smoothedMovement < movementThreshold) {
                        consecutiveLowMovementFrames++
                        if (consecutiveLowMovementFrames >= maxHistorySize) {
                            previousPoseLandmarks = null
                            Log.d(
                                "SleepDetection",
                                "Reset previousPoseLandmarks: Consistently low movement"
                            )
                        }
                    } else {
                        consecutiveLowMovementFrames = 0
                    }
                    0f
                }

            Log.d(
                "SleepDetection",
                "Final movement: $finalMovement, Pose landmarks: ${pose?.allPoseLandmarks?.size ?: 0}, AvgMovementHistory: $avgMovementHistory"
            )

            features = features.copy(movement = finalMovement)

            if (!faceDetected && consecutiveFramesWithoutFace >= 5) {
                features = features.copy(isRollover = true)
                Log.d(
                    "SleepDetection",
                    "Rollover detected: No face for $consecutiveFramesWithoutFace consecutive frames"
                )
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
        val normalizedMovement = if (movement > 15f) movement / 100f else 0f
        Log.d(
            "SleepDetection",
            "Face contour movement: Raw=$movement, Normalized=$normalizedMovement"
        )
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
            ) && it.inFrameLikelihood > 0.6f
        }

        if (previousPoseLandmarks == null || limbLandmarks.size < 3) {
            previousPoseLandmarks = limbLandmarks
            Log.d(
                "SleepDetection",
                "No previous or insufficient limb landmarks (${limbLandmarks.size}), movement=0"
            )
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
                it.landmarkType == current.landmarkType && it.inFrameLikelihood > 0.6f
            }?.let { previous ->
                val distance = sqrt(
                    (current.position.x - previous.position.x).pow(2) +
                            (current.position.y - previous.position.y).pow(2)
                )

                if (current.landmarkType in listOf(
                        PoseLandmark.LEFT_KNEE, PoseLandmark.RIGHT_KNEE,
                        PoseLandmark.LEFT_ANKLE, PoseLandmark.RIGHT_ANKLE
                    )
                ) {
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
        val normalizedMovement = if (weightedMovement > 15f) weightedMovement / 100f else 0f

        Log.d(
            "SleepDetection",
            "Limb movement: Legs=$averageLegMovement, Arms=$averageArmMovement, Weighted=$weightedMovement, Normalized=$normalizedMovement"
        )
        return normalizedMovement
    }

    private fun detectRollover(landmarks: List<PoseLandmark>): Boolean {
        if (landmarks.size < 5) {
            Log.d("SleepDetection", "Rollover skipped: Insufficient landmarks (${landmarks.size})")
            return false
        }

        val nose =
            landmarks.find { it.landmarkType == PoseLandmark.NOSE && it.inFrameLikelihood > 0.8f }
        val leftShoulder =
            landmarks.find { it.landmarkType == PoseLandmark.LEFT_SHOULDER && it.inFrameLikelihood > 0.5f }
        val rightShoulder =
            landmarks.find { it.landmarkType == PoseLandmark.RIGHT_SHOULDER && it.inFrameLikelihood > 0.5f }
        val leftHip =
            landmarks.find { it.landmarkType == PoseLandmark.LEFT_HIP && it.inFrameLikelihood > 0.5f }
        val rightHip =
            landmarks.find { it.landmarkType == PoseLandmark.RIGHT_HIP && it.inFrameLikelihood > 0.5f }

        if (nose == null || nose.inFrameLikelihood < 0.6f) {
            Log.d(
                "SleepDetection",
                "Rollover detected: Nose not visible or low confidence (likelihood=${nose?.inFrameLikelihood})"
            )
            return true
        }

        if (leftShoulder != null && rightShoulder != null && leftHip != null && rightHip != null) {
            val shoulderYDiff = abs(leftShoulder.position.y - rightShoulder.position.y)
            val hipYDiff = abs(leftHip.position.y - rightHip.position.y)
            val isRollover = shoulderYDiff > 50f || hipYDiff > 50f
            Log.d(
                "SleepDetection",
                "Rollover detection from pose: ShoulderYDiff=$shoulderYDiff, HipYDiff=$hipYDiff, Rollover=$isRollover"
            )
            return isRollover
        }

        Log.d("SleepDetection", "Rollover detection: Insufficient pose landmarks")
        return false
    }

    private fun analyzeFeatures(features: SleepFeatures): SleepStatus {
        if (features.eyeAspectRatio == 0f && features.movement == 0f) {
            return SleepStatus(
                eyeStatus = "Mata: Tidak terdeteksi",
                movementStatus = "Pergerakan: Tidak terdeteksi",
                rolloverStatus = if (consecutiveFramesWithoutFace >= 5) "Rollover: Kemungkinan Ya" else "Rollover: Tidak terdeteksi"
            )
        }

        val eyeStatus = when {
            features.eyeAspectRatio > 0.7f -> "Terbuka [${
                String.format(
                    "%.2f",
                    features.eyeAspectRatio
                )
            }]"

            features.eyeAspectRatio > 0.3f -> "Setengah terbuka [${
                String.format(
                    "%.2f",
                    features.eyeAspectRatio
                )
            }]"

            else -> "Tertutup [${String.format("%.2f", features.eyeAspectRatio)}]"
        }

        val movementStatus = when {
            features.movement > 0.4f -> "Bergerak aktif [${
                String.format(
                    "%.3f",
                    features.movement
                )
            }]"

            else -> "Tidak bergerak [${String.format("%.3f", features.movement)}]"
        }

        val rolloverStatus = when {
            features.isRollover && consecutiveFramesWithoutFace >= 5 -> "Ya (wajah tidak terlihat)"
            features.isRollover -> "Kemungkinan Ya"
            else -> "Tidak"
        }

        return SleepStatus(
            eyeStatus = eyeStatus,
            movementStatus = movementStatus,
            rolloverStatus = rolloverStatus
        )
    }

    private fun updateAnalytics(features: SleepFeatures) {
        frameCount++
        totalEAR += features.eyeAspectRatio
        totalMovement += features.movement
        if (features.isRollover) rolloverCount++
        Log.d(
            "SleepDetection",
            "Analytics updated: Frames=$frameCount, AvgEAR=${totalEAR / frameCount}, AvgMovement=${totalMovement / frameCount}"
        )
    }

    fun resetSession() {
        frameCount = 0
        totalEAR = 0f
        totalMovement = 0f
        rolloverCount = 0
        consecutiveFramesWithoutFace = 0
        consecutiveFramesWithFace = 0
        consecutiveLowMovementFrames = 0
        previousPoseLandmarks = null
        previousFaceContours = null
        smoothedMovement = 0f
        movementHistory.clear()
        _sleepStatus.value = SleepStatus()
        Log.d("SleepDetection", "Session reset")
    }

    override fun onCleared() {
        super.onCleared()
        faceDetector.close()
        poseDetector.close()
        liveKitService.disconnect() // Clean up LiveKit connection
        Log.d("SleepDetection", "ViewModel cleared")
    }
}