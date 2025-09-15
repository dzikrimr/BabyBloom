package com.example.bubtrack.presentation.ai.cobamonitor

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bubtrack.data.webrtc.FirebaseClient
import com.example.bubtrack.data.webrtc.IceCandidateDto
import com.example.bubtrack.data.webrtc.MyPeerObserver
import com.example.bubtrack.data.webrtc.RTCClient
import com.example.bubtrack.data.webrtc.RTCClientImpl
import com.example.bubtrack.data.webrtc.WebRTCFactory
import com.google.gson.Gson
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.google.mlkit.vision.pose.PoseDetection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject

@OptIn(ExperimentalGetImage::class)
@HiltViewModel
class MonitorRoomViewModel @Inject constructor(
    private val firebaseClient: FirebaseClient,
    private val webRTCFactory: WebRTCFactory,
    private val gson: Gson,
    private val application: Application
) : ViewModel() {

    companion object { private const val TAG = "MonitorVM" }

    // UI states
    private val _state = MutableStateFlow<MonitorState>(MonitorState.Idle)
    val state: StateFlow<MonitorState> = _state

    private val _isAnalyzingPose = MutableStateFlow(false)
    val isAnalyzingPose: StateFlow<Boolean> = _isAnalyzingPose

    private val _poseState = MutableStateFlow("Unknown")
    val poseState: StateFlow<String> = _poseState

    // internal
    private var rtcClient: RTCClient? = null
    private var remoteSurface: SurfaceViewRenderer? = null
    var roomId: String? = null
        private set
    private var role: String = "parent" // or "baby"

    // ML Kit pose detector (stream mode)
    private val poseDetector by lazy {
        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
            .build()
        PoseDetection.getClient(options)
    }
    private var lastAnalyzedTime = 0L
    private val THROTTLE_MS = 500L // 2 fps

    // --- UI helper ---
    fun initRemoteSurface(renderer: SurfaceViewRenderer) {
        remoteSurface = renderer
        webRTCFactory.initSurfaceView(renderer)
    }

    fun startLocalStream(surface: SurfaceViewRenderer) {
        webRTCFactory.prepareLocalStream(surface)
    }

    // --- Parent flow ---
    fun createRoom(onRoomCreated: (String) -> Unit) {
        role = "parent"
        val newRoomId = firebaseClient.generateRoomId()
        roomId = newRoomId
        _state.value = MonitorState.WaitingForBaby

        viewModelScope.launch {
            firebaseClient.createRoom(newRoomId)
            onRoomCreated(newRoomId)

            // Listen offer from baby
            firebaseClient.observeOffer(newRoomId) { offerSdp ->
                Log.d(TAG, "Parent: Offer received")
                setupRtc(newRoomId)
                rtcClient?.onRemoteSessionReceived(
                    SessionDescription(SessionDescription.Type.OFFER, offerSdp)
                )
                rtcClient?.answer()
                _state.value = MonitorState.Connecting
            }

            // Listen ICE from baby
            firebaseClient.observeIceCandidates(newRoomId, "baby") { candidateJson ->
                runCatching {
                    val dto = gson.fromJson(candidateJson, IceCandidateDto::class.java)
                    rtcClient?.onIceCandidateReceived(
                        IceCandidate(dto.sdpMid, dto.sdpMLineIndex, dto.sdp)
                    )
                }
            }
        }
    }

    fun stopMonitoring(roomId: String) {
        Log.d(TAG, "Parent stops monitoring, notify baby")
        viewModelScope.launch {
            firebaseClient.notifyParentLeft(roomId)
        }
        // stop local peer connection on parent side
        rtcClient?.onDestroy()
        webRTCFactory.onDestroy() // optional on parent side cleanup
        _state.value = MonitorState.Idle
    }

    // --- Baby flow ---
    fun joinRoomAsBaby(roomId: String, localSurface: SurfaceViewRenderer) {
        role = "baby"
        this.roomId = roomId
        _state.value = MonitorState.Connecting

        // start preview + stream (WebRTC)
        webRTCFactory.prepareLocalStream(localSurface)

        // setup rtc
        setupRtc(roomId)

        // create offer
        rtcClient?.offer()

        // listen parent answer
        firebaseClient.observeAnswer(roomId) { answerSdp ->
            // if answer becomes available, still connected
            rtcClient?.onRemoteSessionReceived(
                SessionDescription(SessionDescription.Type.ANSWER, answerSdp)
            )
            _state.value = MonitorState.Connected
        }

        // listen ICE from parent
        firebaseClient.observeIceCandidates(roomId, "parent") { candidateJson ->
            runCatching {
                val dto = gson.fromJson(candidateJson, IceCandidateDto::class.java)
                rtcClient?.onIceCandidateReceived(
                    IceCandidate(dto.sdpMid, dto.sdpMLineIndex, dto.sdp)
                )
            }
        }

        // listen if parent left -> do NOT start CameraX here (no lifecycleOwner), only set flag and free camera
        firebaseClient.observeParentLeft(roomId) {
            Log.d(TAG, "Baby detected parent left → prepare for pose analysis")
            // stop peer connection (free camera), but don't try to start CameraX here
            rtcClient?.onDestroy()
            webRTCFactory.onDestroy() // release WebRTC capture so CameraX can open camera
            _state.value = MonitorState.AnalyzingPose
            _isAnalyzingPose.value = true
            // UI should observe isAnalyzingPose and call startPoseAnalysis(...) with lifecycleOwner/previewView
        }
    }

    // --- RTC setup ---
    private fun setupRtc(roomId: String) {
        runCatching { rtcClient?.onDestroy() }
        rtcClient = webRTCFactory.createRTCClient(
            observer = object : MyPeerObserver() {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    p0?.let { ice ->
                        viewModelScope.launch {
                            val dto = IceCandidateDto(ice.sdpMid, ice.sdpMLineIndex, ice.sdp)
                            firebaseClient.postIceCandidate(roomId, role, gson.toJson(dto))
                        }
                    }
                }

                override fun onAddStream(p0: org.webrtc.MediaStream?) {
                    super.onAddStream(p0)
                    p0?.videoTracks?.firstOrNull()?.let { videoTrack ->
                        remoteSurface?.let { surface ->
                            videoTrack.addSink(surface)
                        }
                    }
                }
            },
            listener = object : RTCClientImpl.TransferDataToServerCallback {
                override fun onIceGenerated(iceCandidate: IceCandidate) {
                    viewModelScope.launch {
                        val dto = IceCandidateDto(
                            iceCandidate.sdpMid,
                            iceCandidate.sdpMLineIndex,
                            iceCandidate.sdp
                        )
                        firebaseClient.postIceCandidate(roomId, role, gson.toJson(dto))
                    }
                }

                override fun onOfferGenerated(sessionDescription: SessionDescription) {
                    viewModelScope.launch {
                        firebaseClient.postOffer(roomId, sessionDescription.description)
                    }
                }

                override fun onAnswerGenerated(sessionDescription: SessionDescription) {
                    viewModelScope.launch {
                        firebaseClient.postAnswer(roomId, sessionDescription.description)
                    }
                }
            }
        )
    }

    // --- Pose analysis (CameraX + MLKit) ---
    /**
     * Start CameraX + ML Kit pose detection.
     * MUST be called from UI with a LifecycleOwner (Activity/Fragment/Compose LocalLifecycleOwner).
     * previewView: optional -> if provided, a live preview is shown. If null, only image analysis runs.
     */
    fun startPoseAnalysis(context: Context, lifecycleOwner: LifecycleOwner, previewView: PreviewView? = null) {
        Log.d(TAG, "startPoseAnalysis() called")
        _state.value = MonitorState.AnalyzingPose
        _isAnalyzingPose.value = true

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = previewView?.let {
                Preview.Builder().build().also { p -> p.setSurfaceProvider(it.surfaceProvider) }
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analyzer ->
                    analyzer.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                        analyzePose(imageProxy)
                    }
                }

            try {
                cameraProvider.unbindAll()
                if (preview != null) {
                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)
                } else {
                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, imageAnalyzer)
                }
            } catch (exc: Exception) {
                Log.e(TAG, "CameraX bind failed: ${exc.message}", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun analyzePose(imageProxy: ImageProxy) {
        val now = System.currentTimeMillis()
        if (now - lastAnalyzedTime < THROTTLE_MS) {
            imageProxy.close()
            return
        }
        lastAnalyzedTime = now

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        poseDetector.process(inputImage)
            .addOnSuccessListener { pose ->
                // simple heuristics
                val nose = pose.getPoseLandmark(PoseLandmark.NOSE)
                val leftEye = pose.getPoseLandmark(PoseLandmark.LEFT_EYE)
                val rightEye = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE)
                val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
                val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)

                val label = when {
                    nose == null && leftShoulder != null && rightShoulder != null -> "Prone (Danger!)"
                    nose != null && leftEye != null && rightEye != null -> "Sleeping"
                    else -> "Normal / Awake"
                }
                _poseState.value = label

                // TODO: jika perlu kirim alert FCM untuk kondisi berbahaya
                // if (label.contains("Prone")) sendPoseAlertToParent(...)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "pose detect failed: ${e.message}")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    // optional helper to programmatically stop pose analysis
    fun stopPoseAnalysis(lifecycleOwner: LifecycleOwner, context: Context) {
        _isAnalyzingPose.value = false
        _state.value = MonitorState.Idle
        // CameraX lifecycle owner unbind handled by CameraX when UI unbinds; if needed:
        ProcessCameraProvider.getInstance(context).addListener({
            val cp = ProcessCameraProvider.getInstance(context).get()
            cp.unbindAll()
        }, ContextCompat.getMainExecutor(context))
    }

    override fun onCleared() {
        super.onCleared()
        rtcClient?.onDestroy()
        webRTCFactory.onDestroy()
        firebaseClient.clear()
        // poseDetector lifecycle managed by ML Kit; you may call close() if desired
    }
}
