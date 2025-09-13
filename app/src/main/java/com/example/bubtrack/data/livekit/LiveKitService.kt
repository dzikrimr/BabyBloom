package com.example.bubtrack.data.livekit

import android.content.Context
import android.util.Log
import io.livekit.android.LiveKit
import io.livekit.android.ConnectOptions
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.participant.RemoteParticipant
import io.livekit.android.room.track.*
import io.livekit.android.room.track.video.CameraCapturerUtils.createCameraCapturer
import io.livekit.android.room.track.LocalVideoTrackOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import livekit.org.webrtc.VideoCapturer
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class BabyMonitorData(
    val eyeStatus: String,
    val movementStatus: String,
    val rolloverStatus: String,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceId: String
)

@Serializable
data class RoomInfo(
    val roomName: String,
    val deviceId: String,
    val deviceName: String,
    val createdAt: Long = System.currentTimeMillis()
)

enum class LiveKitConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    FAILED,
    RECONNECTING
}

@Singleton
class LiveKitService @Inject constructor(
    private val context: Context,
    private val tokenGenerator: TokenGenerator
) {
    private val tag = "LiveKitService"
    private val LIVEKIT_URL = "wss://babybloom-o7dwrf4p.livekit.cloud"

    private var room: Room? = null
    private var localVideoTrack: LocalVideoTrack? = null
    private var localAudioTrack: LocalAudioTrack? = null
    private var videoCapturer: VideoCapturer? = null

    private val _connectionState = MutableStateFlow(LiveKitConnectionState.DISCONNECTED)
    val connectionState: StateFlow<LiveKitConnectionState> = _connectionState.asStateFlow()

    private val _isHost = MutableStateFlow(false)
    val isHost: StateFlow<Boolean> = _isHost.asStateFlow()

    private val _currentRoomName = MutableStateFlow<String?>(null)
    val currentRoomName: StateFlow<String?> = _currentRoomName.asStateFlow()

    private val _remoteBabyData = MutableStateFlow<BabyMonitorData?>(null)
    val remoteBabyData: StateFlow<BabyMonitorData?> = _remoteBabyData.asStateFlow()

    private val _remoteVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val remoteVideoTrack: StateFlow<VideoTrack?> = _remoteVideoTrack.asStateFlow()

    private val _participantCount = MutableStateFlow(1)
    val participantCount: StateFlow<Int> = _participantCount.asStateFlow()

    private val deviceId = UUID.randomUUID().toString()
    private val deviceName = "${android.os.Build.BRAND} ${android.os.Build.MODEL}"
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    init {
        initializeLiveKit()
    }

    private fun initializeLiveKit() {
        try {
            LiveKit.create(context.applicationContext)
            Log.d(tag, "LiveKit initialized successfully")
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize LiveKit: ${e.message}")
        }
    }

    fun generateRoomInfo(): String {
        val roomName = "baby_monitor_${UUID.randomUUID().toString().take(8)}"
        _currentRoomName.value = roomName
        val roomInfo = RoomInfo(roomName = roomName, deviceId = deviceId, deviceName = deviceName)
        Log.d(tag, "Generated room info: $roomName")
        return json.encodeToString(RoomInfo.serializer(), roomInfo)
    }

    fun startAsHostWithRoomInfo(roomInfoString: String): Boolean {
        return try {
            val roomInfo = json.decodeFromString(RoomInfo.serializer(), roomInfoString)
            _isHost.value = true
            _currentRoomName.value = roomInfo.roomName
            connectToRoom(roomInfo.roomName, isHost = true)
            Log.d(tag, "Starting as host with room: ${roomInfo.roomName}")
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to start as host with room info: ${e.message}")
            _connectionState.value = LiveKitConnectionState.FAILED
            false
        }
    }

    fun startAsHost(): String {
        return generateRoomInfo()
    }

    fun joinAsViewer(qrData: String): Boolean {
        return try {
            val roomInfo = json.decodeFromString(RoomInfo.serializer(), qrData)
            if (_connectionState.value != LiveKitConnectionState.DISCONNECTED) {
                Log.d(tag, "Disconnecting from current room before joining as viewer")
                disconnect()
                // Use coroutine scope for delay
                coroutineScope.launch {
                    delay(1000)
                    continueJoinAsViewer(roomInfo)
                }
                return true
            } else {
                return continueJoinAsViewer(roomInfo)
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to parse QR data or join room: ${e.message}")
            _connectionState.value = LiveKitConnectionState.FAILED
            false
        }
    }

    private fun continueJoinAsViewer(roomInfo: RoomInfo): Boolean {
        return try {
            _isHost.value = false
            _currentRoomName.value = roomInfo.roomName
            connectToRoom(roomInfo.roomName, isHost = false)
            Log.d(tag, "Joining room as viewer: ${roomInfo.roomName}")
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to join room as viewer: ${e.message}")
            _connectionState.value = LiveKitConnectionState.FAILED
            false
        }
    }

    private fun connectToRoom(roomName: String, isHost: Boolean) {
        if (_connectionState.value == LiveKitConnectionState.CONNECTING) {
            Log.w(tag, "Already connecting to a room")
            return
        }

        _connectionState.value = LiveKitConnectionState.CONNECTING

        coroutineScope.launch {
            try {
                val identity = if (isHost) "host_$deviceId" else "viewer_$deviceId"
                val token = tokenGenerator.generateAccessToken(roomName, identity, isHost)
                Log.d(tag, "Generated token for $identity: $token")

                val connectOptions = ConnectOptions(autoSubscribe = true)
                room = LiveKit.create(appContext = context.applicationContext)
                room?.connect(
                    url = LIVEKIT_URL,
                    token = token,
                    options = connectOptions
                )
                setupRoomEventListeners()
                Log.d(tag, "Connecting to room: $roomName as ${if (isHost) "host" else "viewer"}")
            } catch (e: Exception) {
                Log.e(tag, "Failed to connect to room: ${e.message}", e)
                _connectionState.value = LiveKitConnectionState.FAILED
            }
        }
    }

    private fun setupRoomEventListeners() {
        room?.let { room ->
            coroutineScope.launch {
                room.events.collect { event ->
                    when (event) {
                        is RoomEvent.Connected -> {
                            Log.d(tag, "Connected to room successfully")
                            _connectionState.value = LiveKitConnectionState.CONNECTED
                            updateParticipantCount()
                            setupLocalTracks()
                        }
                        is RoomEvent.Disconnected -> {
                            Log.d(tag, "Disconnected from room")
                            _connectionState.value = LiveKitConnectionState.DISCONNECTED
                            resetState()
                        }
                        is RoomEvent.Reconnecting -> {
                            Log.d(tag, "Reconnecting to room...")
                            _connectionState.value = LiveKitConnectionState.RECONNECTING
                        }
                        is RoomEvent.Reconnected -> {
                            Log.d(tag, "Reconnected to room")
                            _connectionState.value = LiveKitConnectionState.CONNECTED
                        }
                        is RoomEvent.FailedToConnect -> {
                            Log.e(tag, "Failed to connect: ${event.error}")
                            _connectionState.value = LiveKitConnectionState.FAILED
                        }
                        is RoomEvent.ParticipantConnected -> {
                            Log.d(tag, "Participant connected: ${event.participant.identity}")
                            updateParticipantCount()
                        }
                        is RoomEvent.ParticipantDisconnected -> {
                            Log.d(tag, "Participant disconnected: ${event.participant.identity}")
                            updateParticipantCount()
                        }
                        is RoomEvent.TrackPublished -> {
                            Log.d(tag, "Track published successfully: ${event.publication.track?.sid}, Type: ${event.publication.track?.kind}")
                            if (event.publication.track is VideoTrack) {
                                Log.d(tag, "Video track published successfully for host")
                            }
                            // Use coroutine launch for delay
                            launch {
                                delay(2000)
                                logRoomStatus()
                            }
                        }
                        is RoomEvent.TrackPublicationFailed -> {
                            Log.e(tag, "Track publication failed for track: ${event.track.kind}, Participant: ${event.participant.identity}")
                        }
                        is RoomEvent.TrackSubscribed -> {
                            handleTrackSubscribed(event.track, event.participant)
                        }
                        is RoomEvent.TrackUnsubscribed -> {
                            handleTrackUnsubscribed(event.track, event.participant)
                        }
                        is RoomEvent.DataReceived -> {
                            handleDataReceived(event.data, event.participant ?: return@collect)
                        }
                        is RoomEvent.ConnectionQualityChanged -> {
                            Log.d(tag, "Connection quality changed: ${event.quality}, Participant: ${event.participant.identity}")
                        }
                        else -> {
                            Log.d(tag, "Room event: ${event::class.simpleName}")
                        }
                    }
                }
            }
        }
    }

    private fun setupLocalTracks() {
        coroutineScope.launch {
            try {
                room?.let { room ->
                    val localParticipant = room.localParticipant
                    Log.d(tag, "Setting up local tracks - isHost: ${_isHost.value}")

                    if (_isHost.value) {
                        val hasCameraPermission = android.content.pm.PackageManager.PERMISSION_GRANTED ==
                                androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.CAMERA
                                )
                        if (!hasCameraPermission) {
                            Log.e(tag, "Camera permission not granted, cannot create video track")
                            return@launch
                        }

                        try {
                            Log.d(tag, "Creating video track for host...")
                            val capturerOptions = LocalVideoTrackOptions(
                                position = CameraPosition.BACK
                            )
                            val capturerPair = createCameraCapturer(context, capturerOptions)
                            if (capturerPair == null) {
                                Log.e(tag, "Failed to create camera capturer - null returned")
                                return@launch
                            }
                            videoCapturer = capturerPair.first
                            localVideoTrack = localParticipant.createVideoTrack(
                                name = "camera",
                                capturer = videoCapturer!!,
                                options = capturerOptions
                            )
                            localVideoTrack?.let { videoTrack ->
                                videoTrack.enabled = true
                                Log.d(tag, "Publishing video track...")
                                localParticipant.publishVideoTrack(videoTrack)
                                Log.d(tag, "Video track publication initiated")
                            } ?: run {
                                Log.e(tag, "Failed to create video track - localVideoTrack is null")
                            }
                        } catch (e: Exception) {
                            Log.e(tag, "Failed to setup video track: ${e.message}", e)
                        }
                    } else {
                        Log.d(tag, "Viewer - not publishing video or audio tracks")
                        return@launch
                    }

                    try {
                        Log.d(tag, "Creating audio track...")
                        localAudioTrack = localParticipant.createAudioTrack()
                        localAudioTrack?.let { audioTrack ->
                            audioTrack.enabled = true
                            localParticipant.publishAudioTrack(audioTrack)
                            Log.d(tag, "Local audio track published successfully")
                        } ?: run {
                            Log.e(tag, "Failed to create audio track - localAudioTrack is null")
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to setup audio track: ${e.message}", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to setup local tracks: ${e.message}", e)
            }
        }
    }

    private fun logRoomStatus() {
        room?.let { room ->
            Log.d(tag, "=== ROOM STATUS ===")
            Log.d(tag, "Local participant: ${room.localParticipant.identity}")
            Log.d(tag, "Local video publications: ${room.localParticipant.videoTrackPublications.size}")
            Log.d(tag, "Local audio publications: ${room.localParticipant.audioTrackPublications.size}")
            room.remoteParticipants.values.forEach { participant ->
                Log.d(tag, "Remote participant: ${participant.identity}")
                Log.d(tag, "  Video publications: ${participant.videoTrackPublications.size}")
                Log.d(tag, "  Audio publications: ${participant.audioTrackPublications.size}")
            }
            Log.d(tag, "==================")
        }
    }

    private fun handleTrackSubscribed(track: Track, participant: RemoteParticipant?) {
        participant ?: return
        when (track) {
            is VideoTrack -> {
                Log.d(tag, "Remote video track subscribed from ${participant.identity}, Track SID: ${track.sid}, Enabled: ${track.enabled}")
                _remoteVideoTrack.value = track
                Log.d(tag, "Remote video track set successfully")
            }
            is AudioTrack -> {
                Log.d(tag, "Remote audio track subscribed from ${participant.identity}, Track SID: ${track.sid}, Enabled: ${track.enabled}")
            }
        }
    }

    private fun handleTrackUnsubscribed(track: Track, participant: RemoteParticipant?) {
        participant ?: return
        when (track) {
            is VideoTrack -> {
                Log.d(tag, "Remote video track unsubscribed from ${participant.identity}")
                if (_remoteVideoTrack.value == track) {
                    _remoteVideoTrack.value = null
                }
            }
            is AudioTrack -> {
                Log.d(tag, "Remote audio track unsubscribed from ${participant.identity}")
            }
        }
    }

    private fun handleDataReceived(data: ByteArray, participant: RemoteParticipant) {
        try {
            val message = String(data, Charsets.UTF_8)
            val babyData = json.decodeFromString(BabyMonitorData.serializer(), message)
            _remoteBabyData.value = babyData
            Log.d(tag, "Received baby monitor data from ${participant.identity}: ${babyData.eyeStatus}")
        } catch (e: Exception) {
            Log.e(tag, "Failed to parse received data: ${e.message}")
        }
    }

    private fun updateParticipantCount() {
        room?.let { room ->
            val count = room.remoteParticipants.size + 1
            _participantCount.value = count
            Log.d(tag, "Participant count updated: $count")
        }
    }

    private fun resetState() {
        _participantCount.value = 1
        _remoteVideoTrack.value = null
        _remoteBabyData.value = null
        localVideoTrack = null
        localAudioTrack = null
        videoCapturer = null
    }

    fun sendBabyMonitorData(sleepStatus: com.example.bubtrack.models.SleepStatus) {
        room?.let { room ->
            try {
                val data = BabyMonitorData(
                    eyeStatus = sleepStatus.eyeStatus,
                    movementStatus = sleepStatus.movementStatus,
                    rolloverStatus = sleepStatus.rolloverStatus,
                    deviceId = deviceId
                )
                val message = json.encodeToString(BabyMonitorData.serializer(), data)
                coroutineScope.launch {
                    try {
                        room.localParticipant.publishData(message.toByteArray(Charsets.UTF_8))
                        Log.d(tag, "Baby monitor data sent successfully")
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to send baby monitor data: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to serialize baby monitor data: ${e.message}")
            }
        }
    }

    fun getLocalVideoTrack(): LocalVideoTrack? = localVideoTrack

    fun switchCamera() {
        if (_isHost.value && localVideoTrack != null && videoCapturer != null) {
            try {
                Log.w(tag, "Camera switching not supported in this implementation")
            } catch (e: Exception) {
                Log.e(tag, "Failed to switch camera: ${e.message}")
            }
        }
    }

    fun toggleVideo(): Boolean {
        localVideoTrack?.let { track ->
            try {
                track.enabled = !track.enabled
                Log.d(tag, "Video toggled: ${track.enabled}")
                return track.enabled
            } catch (e: Exception) {
                Log.e(tag, "Failed to toggle video: ${e.message}")
            }
        }
        return false
    }

    fun toggleAudio(): Boolean {
        localAudioTrack?.let { track ->
            try {
                track.enabled = !track.enabled
                Log.d(tag, "Audio toggled: ${track.enabled}")
                return track.enabled
            } catch (e: Exception) {
                Log.e(tag, "Failed to toggle audio: ${e.message}")
            }
        }
        return false
    }

    fun disconnect() {
        coroutineScope.launch {
            try {
                localVideoTrack?.let {
                    it.enabled = false
                    it.stop()
                }
                localAudioTrack?.let {
                    it.enabled = false
                    it.stop()
                }
                videoCapturer?.stopCapture()
                videoCapturer = null
                room?.disconnect()
                room = null
                _connectionState.value = LiveKitConnectionState.DISCONNECTED
                _currentRoomName.value = null
                _isHost.value = false
                resetState()
                Log.d(tag, "Successfully disconnected and cleaned up")
            } catch (e: Exception) {
                Log.e(tag, "Error during disconnect: ${e.message}")
            }
        }
    }

    fun getConnectionStats(): Map<String, Any> {
        return room?.let {
            mapOf(
                "connected" to (connectionState.value == LiveKitConnectionState.CONNECTED),
                "participantCount" to participantCount.value,
                "isHost" to isHost.value,
                "roomName" to (currentRoomName.value ?: ""),
                "deviceId" to deviceId
            )
        } ?: emptyMap()
    }
}