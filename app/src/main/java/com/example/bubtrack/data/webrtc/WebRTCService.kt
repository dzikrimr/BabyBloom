package com.example.bubtrack.data.webrtc

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.webrtc.*
import org.webrtc.audio.JavaAudioDeviceModule
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class SignalingMessage(
    val type: String,
    val data: String,
    val deviceId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class PairingInfo(
    val deviceId: String,
    val deviceName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class SleepStatusSync(
    val eyeStatus: String,
    val movementStatus: String,
    val rolloverStatus: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    FAILED
}

@Singleton
class WebRTCService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tag = "WebRTCService"

    private var isLocalRendererInitialized = false
    private var isRemoteRendererInitialized = false

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localVideoTrack: VideoTrack? = null
    private var remoteVideoTrack: VideoTrack? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var videoSource: VideoSource? = null
    private var audioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    var eglBase: EglBase? = try {
        EglBase.create()
    } catch (e: Exception) {
        Log.e(tag, "Failed to create EglBase: ${e.message}")
        null
    }
    private var dataChannel: DataChannel? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _isHost = MutableStateFlow(false)
    val isHost: StateFlow<Boolean> = _isHost.asStateFlow()

    private val _pairedDeviceInfo = MutableStateFlow<PairingInfo?>(null)
    val pairedDeviceInfo: StateFlow<PairingInfo?> = _pairedDeviceInfo.asStateFlow()

    private val _remoteSleepStatus = MutableStateFlow<SleepStatusSync?>(null)
    val remoteSleepStatus: StateFlow<SleepStatusSync?> = _remoteSleepStatus.asStateFlow()

    private val _localRenderer = MutableStateFlow<SurfaceViewRenderer?>(null)
    val localRenderer: StateFlow<SurfaceViewRenderer?> = _localRenderer.asStateFlow()

    private val _remoteRenderer = MutableStateFlow<SurfaceViewRenderer?>(null)
    val remoteRenderer: StateFlow<SurfaceViewRenderer?> = _remoteRenderer.asStateFlow()

    private val deviceId = UUID.randomUUID().toString()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    // Simulated signaling server (in real implementation, use WebSocket or Socket.IO)
    private val _signalingMessages = MutableStateFlow<List<SignalingMessage>>(emptyList())
    val signalingMessages: StateFlow<List<SignalingMessage>> = _signalingMessages.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }

    init {
        initializeWebRTC()
    }

    private fun initializeWebRTC() {
        try {
            val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(initializationOptions)

            val audioDeviceModule = JavaAudioDeviceModule.builder(context)
                .createAudioDeviceModule()

            peerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase?.eglBaseContext, true, true))
                .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase?.eglBaseContext))
                .setAudioDeviceModule(audioDeviceModule)
                .createPeerConnectionFactory()

            Log.d(tag, "WebRTC initialized successfully")
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize WebRTC: ${e.message}", e)
            _connectionState.value = ConnectionState.FAILED
        }
    }

    fun startAsHost(localRenderer: SurfaceViewRenderer): String {
        try {
            _isHost.value = true
            _connectionState.value = ConnectionState.CONNECTING

            initializeLocalVideo(localRenderer)
            createPeerConnection()

            val pairingInfo = PairingInfo(
                deviceId = deviceId,
                deviceName = android.os.Build.MODEL
            )

            return json.encodeToString(PairingInfo.serializer(), pairingInfo)
        } catch (e: Exception) {
            Log.e(tag, "Failed to start as host: ${e.message}", e)
            _connectionState.value = ConnectionState.FAILED
            return ""
        }
    }

    fun joinAsClient(qrData: String, localRenderer: SurfaceViewRenderer, remoteRenderer: SurfaceViewRenderer) {
        try {
            val pairingInfo = json.decodeFromString(PairingInfo.serializer(), qrData)
            _pairedDeviceInfo.value = pairingInfo
            _isHost.value = false
            _connectionState.value = ConnectionState.CONNECTING

            _localRenderer.value = localRenderer
            _remoteRenderer.value = remoteRenderer
            initializeLocalVideo(localRenderer)
            initializeRemoteVideo(remoteRenderer)
            createPeerConnection()

            // Send join request
            sendSignalingMessage(
                SignalingMessage(
                    type = "join_request",
                    data = json.encodeToString(
                        PairingInfo.serializer(),
                        PairingInfo(
                            deviceId = deviceId,
                            deviceName = android.os.Build.MODEL
                        )
                    ),
                    deviceId = deviceId
                )
            )
        } catch (e: Exception) {
            Log.e(tag, "Failed to join as client: ${e.message}", e)
            _connectionState.value = ConnectionState.FAILED
        }
    }

    private fun initializeLocalVideo(renderer: SurfaceViewRenderer) {
        try {
            if (_localRenderer.value == renderer && isLocalRendererInitialized) {
                Log.d(tag, "Local renderer already initialized, skipping init")
            } else {
                renderer.init(eglBase?.eglBaseContext, null)
                renderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                renderer.setMirror(true)
                _localRenderer.value = renderer
                isLocalRendererInitialized = true
                Log.d(tag, "Local renderer initialized")
            }

            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase?.eglBaseContext)
            videoSource = peerConnectionFactory?.createVideoSource(false)

            val cameraEnumerator = Camera2Enumerator(context)
            val deviceNames = cameraEnumerator.deviceNames
            Log.d(tag, "Available cameras: ${deviceNames.joinToString()}")

            var selectedCamera: String? = null
            var cameraInitialized = false

            // Try each available camera
            for (deviceName in deviceNames) {
                try {
                    if (cameraEnumerator.isBackFacing(deviceName)) {
                        selectedCamera = deviceName
                    } else if (selectedCamera == null) {
                        selectedCamera = deviceName // Fallback to any camera
                    }

                    if (selectedCamera != null) {
                        videoCapturer = cameraEnumerator.createCapturer(selectedCamera, null)
                        videoCapturer?.initialize(surfaceTextureHelper, context, videoSource?.capturerObserver)
                        videoCapturer?.startCapture(640, 480, 30)

                        localVideoTrack = peerConnectionFactory?.createVideoTrack("local_video", videoSource)
                        localVideoTrack?.addSink(renderer)

                        Log.d(tag, "Local video initialized with camera: $selectedCamera")
                        cameraInitialized = true
                        break
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Failed to initialize camera $selectedCamera: ${e.message}")
                    selectedCamera = null // Try next camera
                }
            }

            if (!cameraInitialized) {
                Log.e(tag, "No camera could be initialized")
                _connectionState.value = ConnectionState.FAILED
                return
            }

            audioSource = peerConnectionFactory?.createAudioSource(MediaConstraints())
            localAudioTrack = peerConnectionFactory?.createAudioTrack("local_audio", audioSource)
            Log.d(tag, "Audio initialized")
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize local video: ${e.message}", e)
            _connectionState.value = ConnectionState.FAILED
        }
    }

    private fun initializeRemoteVideo(renderer: SurfaceViewRenderer) {
        try {
            renderer.init(eglBase?.eglBaseContext, null)
            renderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
            renderer.setMirror(false)
            _remoteRenderer.value = renderer
            Log.d(tag, "Remote video renderer initialized")
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize remote video: ${e.message}", e)
            _connectionState.value = ConnectionState.FAILED
        }
    }

    private fun handleAnswer(answer: SessionDescriptionData) {
        try {
            val sessionDescription = SessionDescription(
                SessionDescription.Type.fromCanonicalForm(answer.type),
                answer.sdp
            )

            peerConnection?.setRemoteDescription(object : SdpObserver {
                override fun onCreateSuccess(p0: SessionDescription?) {}
                override fun onSetSuccess() {
                    Log.d(tag, "Remote description set successfully")
                }
                override fun onCreateFailure(error: String?) {}
                override fun onSetFailure(error: String?) {
                    Log.e(tag, "Set remote description failed: $error")
                    _connectionState.value = ConnectionState.FAILED
                }
            }, sessionDescription)
        } catch (e: Exception) {
            Log.e(tag, "Failed to handle answer: ${e.message}", e)
            _connectionState.value = ConnectionState.FAILED
        }
    }

    private fun createPeerConnection() {
        try {
            val iceServers = listOf(
                PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
            )

            val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
                tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
                bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
                rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
                continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
                sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            }

            peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
                override fun onSignalingChange(state: PeerConnection.SignalingState?) {
                    Log.d(tag, "Signaling state changed: $state")
                }

                override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                    Log.d(tag, "ICE connection state changed: $state")
                    when (state) {
                        PeerConnection.IceConnectionState.CONNECTED -> {
                            _connectionState.value = ConnectionState.CONNECTED
                        }
                        PeerConnection.IceConnectionState.DISCONNECTED -> {
                            _connectionState.value = ConnectionState.DISCONNECTED
                        }
                        PeerConnection.IceConnectionState.FAILED -> {
                            _connectionState.value = ConnectionState.FAILED
                        }
                        else -> {}
                    }
                }

                override fun onIceConnectionReceivingChange(receiving: Boolean) {
                    Log.d(tag, "ICE connection receiving changed: $receiving")
                }

                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {
                    Log.d(tag, "ICE gathering state changed: $state")
                }

                override fun onIceCandidate(candidate: IceCandidate?) {
                    candidate?.let {
                        sendSignalingMessage(
                            SignalingMessage(
                                type = "ice_candidate",
                                data = json.encodeToString(
                                    IceCandidateData.serializer(),
                                    IceCandidateData(it.sdp, it.sdpMLineIndex, it.sdpMid ?: "")
                                ),
                                deviceId = deviceId
                            )
                        )
                    }
                }

                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {
                    Log.d(tag, "ICE candidates removed")
                }

                override fun onAddStream(stream: MediaStream?) {
                    Log.w(tag, "onAddStream called but not used with Unified Plan")
                }

                override fun onRemoveStream(stream: MediaStream?) {
                    Log.d(tag, "Stream removed")
                }

                override fun onDataChannel(dataChannel: DataChannel?) {
                    Log.d(tag, "Data channel received")
                    setupDataChannel(dataChannel)
                }

                override fun onRenegotiationNeeded() {
                    Log.d(tag, "Renegotiation needed")
                }

                override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {
                    Log.d(tag, "Track added")
                    receiver?.track()?.let { track ->
                        if (track is VideoTrack) {
                            remoteVideoTrack = track
                            _remoteRenderer.value?.let { renderer ->
                                track.addSink(renderer)
                                Log.d(tag, "Remote video track added to renderer")
                            }
                        }
                    }
                }
            })

            // Add tracks instead of stream
            localVideoTrack?.let { track ->
                peerConnection?.addTrack(track, listOf("local_stream"))
                Log.d(tag, "Local video track added to PeerConnection")
            }
            localAudioTrack?.let { track ->
                peerConnection?.addTrack(track, listOf("local_stream"))
                Log.d(tag, "Local audio track added to PeerConnection")
            }

            // Create data channel for status sync
            val dataChannelConfig = DataChannel.Init().apply {
                ordered = true
            }
            dataChannel = peerConnection?.createDataChannel("status_sync", dataChannelConfig)
            setupDataChannel(dataChannel)

            // Create offer for host
            if (_isHost.value) {
                createOffer()
            }

            Log.d(tag, "ThirdConnection created successfully")
        } catch (e: Exception) {
            Log.e(tag, "Failed to create PeerConnection: ${e.message}", e)
            _connectionState.value = ConnectionState.FAILED
        }
    }

    private fun setupDataChannel(dataChannel: DataChannel?) {
        dataChannel?.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(amount: Long) {}

            override fun onStateChange() {
                Log.d(tag, "Data channel state: ${dataChannel.state()}")
            }

            override fun onMessage(buffer: DataChannel.Buffer?) {
                buffer?.let {
                    val data = ByteArray(it.data.remaining())
                    it.data.get(data)
                    val message = String(data)
                    try {
                        val statusSync = json.decodeFromString(SleepStatusSync.serializer(), message)
                        _remoteSleepStatus.value = statusSync
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to parse status sync message: ${e.message}")
                    }
                }
            }
        })
    }

    fun syncSleepStatus(status: com.example.bubtrack.models.SleepStatus) {
        try {
            val statusSync = SleepStatusSync(
                eyeStatus = status.eyeStatus,
                movementStatus = status.movementStatus,
                rolloverStatus = status.rolloverStatus
            )

            val message = json.encodeToString(SleepStatusSync.serializer(), statusSync)
            val buffer = DataChannel.Buffer(
                java.nio.ByteBuffer.wrap(message.toByteArray()),
                false
            )

            dataChannel?.let {
                if (it.state() == DataChannel.State.OPEN) {
                    it.send(buffer)
                    Log.d(tag, "Sleep status synced: $message")
                } else {
                    Log.w(tag, "Data channel not open, cannot send status")
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to sync sleep status: ${e.message}", e)
        }
    }

    private fun sendSignalingMessage(message: SignalingMessage) {
        // In real implementation, send via WebSocket to signaling server
        // For this example, we'll simulate with local state
        val currentMessages = _signalingMessages.value.toMutableList()
        currentMessages.add(message)
        _signalingMessages.value = currentMessages

        Log.d(tag, "Signaling message sent: ${message.type}")
    }

    fun processSignalingMessage(message: SignalingMessage) {
        coroutineScope.launch {
            try {
                when (message.type) {
                    "join_request" -> {
                        if (_isHost.value) {
                            val pairingInfo = json.decodeFromString(PairingInfo.serializer(), message.data)
                            _pairedDeviceInfo.value = pairingInfo
                            createOffer()
                        }
                    }
                    "offer" -> {
                        val offer = json.decodeFromString(SessionDescriptionData.serializer(), message.data)
                        handleOffer(offer)
                    }
                    "answer" -> {
                        val answer = json.decodeFromString(SessionDescriptionData.serializer(), message.data)
                        handleAnswer(answer)
                    }
                    "ice_candidate" -> {
                        val candidate = json.decodeFromString(IceCandidateData.serializer(), message.data)
                        handleIceCandidate(candidate)
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to process signaling message: ${e.message}", e)
                _connectionState.value = ConnectionState.FAILED
            }
        }
    }

    private fun createOffer() {
        try {
            val constraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            }

            peerConnection?.createOffer(object : SdpObserver {
                override fun onCreateSuccess(sessionDescription: SessionDescription?) {
                    sessionDescription?.let {
                        peerConnection?.setLocalDescription(object : SdpObserver {
                            override fun onCreateSuccess(p0: SessionDescription?) {}
                            override fun onSetSuccess() {
                                sendSignalingMessage(
                                    SignalingMessage(
                                        type = "offer",
                                        data = json.encodeToString(
                                            SessionDescriptionData.serializer(),
                                            SessionDescriptionData(it.type.canonicalForm(), it.description)
                                        ),
                                        deviceId = deviceId
                                    )
                                )
                            }
                            override fun onCreateFailure(error: String?) {
                                Log.e(tag, "Set local description failed: $error")
                                _connectionState.value = ConnectionState.FAILED
                            }
                            override fun onSetFailure(error: String?) {
                                Log.e(tag, "Set local description failed: $error")
                                _connectionState.value = ConnectionState.FAILED
                            }
                        }, it)
                    }
                }
                override fun onSetSuccess() {}
                override fun onCreateFailure(error: String?) {
                    Log.e(tag, "Create offer failed: $error")
                    _connectionState.value = ConnectionState.FAILED
                }
                override fun onSetFailure(error: String?) {}
            }, constraints)
        } catch (e: Exception) {
            Log.e(tag, "Failed to create offer: ${e.message}", e)
            _connectionState.value = ConnectionState.FAILED
        }
    }

    private fun handleOffer(offer: SessionDescriptionData) {
        try {
            val sessionDescription = SessionDescription(
                SessionDescription.Type.fromCanonicalForm(offer.type),
                offer.sdp
            )

            peerConnection?.setRemoteDescription(object : SdpObserver {
                override fun onCreateSuccess(p0: SessionDescription?) {}
                override fun onSetSuccess() {
                    createAnswer()
                }
                override fun onCreateFailure(error: String?) {}
                override fun onSetFailure(error: String?) {
                    Log.e(tag, "Set remote description failed: $error")
                    _connectionState.value = ConnectionState.FAILED
                }
            }, sessionDescription)
        } catch (e: Exception) {
            Log.e(tag, "Failed to handle offer: ${e.message}", e)
            _connectionState.value = ConnectionState.FAILED
        }
    }

    private fun createAnswer() {
        try {
            val constraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            }

            peerConnection?.createAnswer(object : SdpObserver {
                override fun onCreateSuccess(sessionDescription: SessionDescription?) {
                    sessionDescription?.let {
                        peerConnection?.setLocalDescription(object : SdpObserver {
                            override fun onCreateSuccess(p0: SessionDescription?) {}
                            override fun onSetSuccess() {
                                sendSignalingMessage(
                                    SignalingMessage(
                                        type = "answer",
                                        data = json.encodeToString(
                                            SessionDescriptionData.serializer(),
                                            SessionDescriptionData(it.type.canonicalForm(), it.description)
                                        ),
                                        deviceId = deviceId
                                    )
                                )
                            }
                            override fun onCreateFailure(error: String?) {}
                            override fun onSetFailure(error: String?) {
                                Log.e(tag, "Set local description failed: $error")
                                _connectionState.value = ConnectionState.FAILED
                            }
                        }, it)
                    }
                }
                override fun onSetSuccess() {}
                override fun onCreateFailure(error: String?) {
                    Log.e(tag, "Create answer failed: $error")
                    _connectionState.value = ConnectionState.FAILED
                }
                override fun onSetFailure(error: String?) {}
            }, constraints)
        } catch (e: Exception) {
            Log.e(tag, "Failed to create answer: ${e.message}", e)
            _connectionState.value = ConnectionState.FAILED
        }
    }

    private fun handleIceCandidate(candidateData: IceCandidateData) {
        try {
            val iceCandidate = IceCandidate(candidateData.sdpMid, candidateData.sdpMLineIndex, candidateData.sdp)
            peerConnection?.addIceCandidate(iceCandidate)
            Log.d(tag, "ICE candidate added: ${candidateData.sdp}")
        } catch (e: Exception) {
            Log.e(tag, "Failed to handle ICE candidate: ${e.message}", e)
            _connectionState.value = ConnectionState.FAILED
        }
    }

    fun disconnect() {
        try {
            videoCapturer?.stopCapture()
            peerConnection?.close()
            _connectionState.value = ConnectionState.DISCONNECTED
            _pairedDeviceInfo.value = null
            _remoteSleepStatus.value = null

            localVideoTrack?.dispose()
            remoteVideoTrack?.dispose()
            videoSource?.dispose()
            audioSource?.dispose()
            localAudioTrack?.dispose()
            surfaceTextureHelper?.dispose()
            dataChannel?.dispose()

            localVideoTrack = null
            remoteVideoTrack = null
            videoSource = null
            audioSource = null
            localAudioTrack = null
            videoCapturer = null
            dataChannel = null

            Log.d(tag, "WebRTC disconnected and cleaned up")
        } catch (e: Exception) {
            Log.e(tag, "Failed to disconnect: ${e.message}", e)
        }
    }

    fun cleanup() {
        try {
            disconnect()
            _localRenderer.value?.release()
            _remoteRenderer.value?.release()
            _localRenderer.value = null
            _remoteRenderer.value = null
            isLocalRendererInitialized = false
            isRemoteRendererInitialized = false
            peerConnection?.dispose()
            peerConnectionFactory?.dispose()
            eglBase?.release()
            peerConnection = null
            peerConnectionFactory = null
            eglBase = null
            Log.d(tag, "WebRTC resources fully cleaned up")
        } catch (e: Exception) {
            Log.e(tag, "Failed to cleanup: ${e.message}", e)
        }
    }
}

@Serializable
data class SessionDescriptionData(
    val type: String,
    val sdp: String
)

@Serializable
data class IceCandidateData(
    val sdp: String,
    val sdpMLineIndex: Int,
    val sdpMid: String
)