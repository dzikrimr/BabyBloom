package com.example.bubtrack.data.webrtc


data class IceCandidateDto(
    val sdpMid: String? = null,
    val sdpMLineIndex: Int = 0,
    val sdp: String = ""
)
