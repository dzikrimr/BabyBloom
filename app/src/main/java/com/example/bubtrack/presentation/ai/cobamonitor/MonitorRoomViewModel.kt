package com.example.bubtrack.presentation.ai.cobamonitor

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bubtrack.data.webrtc.FirebaseClient
import com.example.bubtrack.data.webrtc.IceCandidateDto
import com.example.bubtrack.data.webrtc.MyPeerObserver
import com.example.bubtrack.data.webrtc.RTCClient
import com.example.bubtrack.data.webrtc.RTCClientImpl
import com.example.bubtrack.data.webrtc.WebRTCFactory
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject

@HiltViewModel
class MonitorRoomViewModel @Inject constructor(
    private val firebaseClient: FirebaseClient,
    private val webRTCFactory: WebRTCFactory,
    private val gson: Gson,
    private val application: Application
) : ViewModel() {

    companion object { private const val TAG = "MonitorVM" }

    private val _state = MutableStateFlow<MonitorState>(MonitorState.Idle)
    val state: StateFlow<MonitorState> = _state

    private var rtcClient: RTCClient? = null
    private var remoteSurface: SurfaceViewRenderer? = null
    var roomId: String? = null
        private set

    private var role: String = "parent" // or "baby"

    // --- UI helper ---
    fun initRemoteSurface(renderer: SurfaceViewRenderer) {
        Log.d(TAG, "initRemoteSurface called")
        remoteSurface = renderer
        webRTCFactory.initSurfaceView(renderer)
        Log.d(TAG, "Remote surface initialized: ${remoteSurface != null}")
    }

    fun startLocalStream(surface: SurfaceViewRenderer) {
        Log.d(TAG, "startLocalStream called")
        webRTCFactory.prepareLocalStream(surface)
        Log.d(TAG, "Local stream started")
    }

    // --- Parent flow ---
    fun createRoom(onRoomCreated: (String) -> Unit) {
        Log.d(TAG, "createRoom called")
        role = "parent"
        val newRoomId = firebaseClient.generateRoomId()
        roomId = newRoomId
        _state.value = MonitorState.WaitingForBaby
        Log.d(TAG, "Room created with ID: $newRoomId, state: WaitingForBaby")

        viewModelScope.launch {
            try {
                firebaseClient.createRoom(newRoomId)
                onRoomCreated(newRoomId)
                Log.d(TAG, "Firebase room created successfully")

                // Listen offer from baby
                firebaseClient.observeOffer(newRoomId) { offerSdp ->
                    Log.d(TAG, "Parent: Offer received, SDP length: ${offerSdp.length}")
                    setupRtc(newRoomId)
                    rtcClient?.onRemoteSessionReceived(
                        SessionDescription(SessionDescription.Type.OFFER, offerSdp)
                    )
                    rtcClient?.answer()
                    _state.value = MonitorState.Connecting
                    Log.d(TAG, "Parent: Answer sent, state: Connecting")
                }

                // Listen ICE from baby
                firebaseClient.observeIceCandidates(newRoomId, "baby") { candidateJson ->
                    Log.d(TAG, "Parent: ICE candidate received from baby")
                    runCatching {
                        val dto = gson.fromJson(candidateJson, IceCandidateDto::class.java)
                        rtcClient?.onIceCandidateReceived(
                            IceCandidate(dto.sdpMid, dto.sdpMLineIndex, dto.sdp)
                        )
                        Log.d(TAG, "Parent: ICE candidate processed")
                    }.onFailure { e ->
                        Log.e(TAG, "Parent: Failed to process ICE candidate", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating room", e)
                _state.value = MonitorState.Error("Failed to create room: ${e.message}")
            }
        }
    }

    // --- Baby flow ---
    fun joinRoomAsBaby(roomId: String, localSurface: SurfaceViewRenderer) {
        Log.d(TAG, "joinRoomAsBaby called with roomId: $roomId")
        role = "baby"
        this.roomId = roomId
        _state.value = MonitorState.Connecting
        Log.d(TAG, "Baby: State set to Connecting")

        try {
            // start preview + stream
            webRTCFactory.prepareLocalStream(localSurface)
            Log.d(TAG, "Baby: Local stream prepared")

            // setup rtc
            setupRtc(roomId)
            Log.d(TAG, "Baby: RTC setup completed")

            // create offer
            rtcClient?.offer()
            Log.d(TAG, "Baby: Offer created and sent")

            // listen parent answer
            firebaseClient.observeAnswer(roomId) { answerSdp ->
                Log.d(TAG, "Baby: Answer received from parent, SDP length: ${answerSdp.length}")
                rtcClient?.onRemoteSessionReceived(
                    SessionDescription(SessionDescription.Type.ANSWER, answerSdp)
                )
                _state.value = MonitorState.Connected
                Log.d(TAG, "Baby: Connected to parent")
            }

            // listen ICE from parent
            firebaseClient.observeIceCandidates(roomId, "parent") { candidateJson ->
                Log.d(TAG, "Baby: ICE candidate received from parent")
                runCatching {
                    val dto = gson.fromJson(candidateJson, IceCandidateDto::class.java)
                    rtcClient?.onIceCandidateReceived(
                        IceCandidate(dto.sdpMid, dto.sdpMLineIndex, dto.sdp)
                    )
                    Log.d(TAG, "Baby: ICE candidate processed")
                }.onFailure { e ->
                    Log.e(TAG, "Baby: Failed to process ICE candidate", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error joining room as baby", e)
            _state.value = MonitorState.Error("Failed to join room: ${e.message}")
        }
    }

    // --- RTC setup ---
    private fun setupRtc(roomId: String) {
        Log.d(TAG, "setupRtc called for roomId: $roomId, role: $role")

        // Clean up existing connection
        runCatching {
            rtcClient?.onDestroy()
            Log.d(TAG, "Previous RTC client destroyed")
        }

        rtcClient = webRTCFactory.createRTCClient(
            observer = object : MyPeerObserver() {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    Log.d(TAG, "onIceCandidate called")
                    p0?.let { ice ->
                        viewModelScope.launch {
                            val dto = IceCandidateDto(ice.sdpMid, ice.sdpMLineIndex, ice.sdp)
                            firebaseClient.postIceCandidate(roomId, role, gson.toJson(dto))
                            Log.d(TAG, "ICE candidate posted to Firebase")
                        }
                    }
                }

                override fun onAddStream(p0: org.webrtc.MediaStream?) {
                    super.onAddStream(p0)
                    Log.d(TAG, "onAddStream called, stream: ${p0 != null}, videoTracks: ${p0?.videoTracks?.size}")
                    p0?.videoTracks?.firstOrNull()?.let { videoTrack ->
                        remoteSurface?.let { surface ->
                            Log.d(TAG, "Adding video track to remote surface")
                            videoTrack.addSink(surface)
                            Log.d(TAG, "Video track added to surface successfully")
                        } ?: Log.e(TAG, "Remote surface is null!")
                    } ?: Log.e(TAG, "No video tracks in stream!")
                }

                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                    super.onConnectionChange(newState)
                    Log.d(TAG, "Connection state changed to: $newState")
                    if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
                        _state.value = MonitorState.Connected
                        Log.d(TAG, "WebRTC connection established")
                    }
                }
            },
            listener = object : RTCClientImpl.TransferDataToServerCallback {
                override fun onIceGenerated(iceCandidate: IceCandidate) {
                    Log.d(TAG, "onIceGenerated called")
                    viewModelScope.launch {
                        val dto = IceCandidateDto(
                            iceCandidate.sdpMid,
                            iceCandidate.sdpMLineIndex,
                            iceCandidate.sdp
                        )
                        firebaseClient.postIceCandidate(roomId, role, gson.toJson(dto))
                        Log.d(TAG, "Generated ICE candidate posted")
                    }
                }

                override fun onOfferGenerated(sessionDescription: SessionDescription) {
                    Log.d(TAG, "onOfferGenerated called, SDP length: ${sessionDescription.description.length}")
                    viewModelScope.launch {
                        firebaseClient.postOffer(roomId, sessionDescription.description)
                        Log.d(TAG, "Offer posted to Firebase")
                    }
                }

                override fun onAnswerGenerated(sessionDescription: SessionDescription) {
                    Log.d(TAG, "onAnswerGenerated called, SDP length: ${sessionDescription.description.length}")
                    viewModelScope.launch {
                        firebaseClient.postAnswer(roomId, sessionDescription.description)
                        Log.d(TAG, "Answer posted to Firebase")
                    }
                }
            }
        )

        Log.d(TAG, "RTC client created: ${rtcClient != null}")
    }

    fun switchCamera() {
        Log.d(TAG, "switchCamera called")
        webRTCFactory.switchCamera()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel onCleared")
        rtcClient?.onDestroy()
        webRTCFactory.onDestroy()
        firebaseClient.clear()
    }
}