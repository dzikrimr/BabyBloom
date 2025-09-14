package com.example.bubtrack.data.webrtc

data class StatusDataModel(
    val participant: String? = null,
    val type: StatusDataModelType? = null,
)

enum class StatusDataModelType {
    IDLE,
    LookingForMatch,
    OfferedMatch,
    ReceivedMatch,
    Connected
}
