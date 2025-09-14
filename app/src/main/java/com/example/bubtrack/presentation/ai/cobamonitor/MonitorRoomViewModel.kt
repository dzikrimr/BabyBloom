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

    // --- Baby flow ---
    fun joinRoomAsBaby(roomId: String, localSurface: SurfaceViewRenderer) {
        role = "baby"
        this.roomId = roomId
        _state.value = MonitorState.Connecting

        // start preview + stream
        webRTCFactory.prepareLocalStream(localSurface)

        // setup rtc
        setupRtc(roomId)

        // create offer
        rtcClient?.offer()

        // listen parent answer
        firebaseClient.observeAnswer(roomId) { answerSdp ->
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


    private fun postIceCandidate(roomId: String, ice: IceCandidate) {
        viewModelScope.launch {
            val dto = IceCandidateDto(ice.sdpMid, ice.sdpMLineIndex, ice.sdp)
            firebaseClient.postIceCandidate(roomId, role, gson.toJson(dto))
        }
    }

    fun switchCamera() = webRTCFactory.switchCamera()

    override fun onCleared() {
        super.onCleared()
        rtcClient?.onDestroy()
        webRTCFactory.onDestroy()
        firebaseClient.clear()
    }
}
