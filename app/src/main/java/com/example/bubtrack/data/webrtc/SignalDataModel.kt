package com.example.bubtrack.data.webrtc

data class SignalDataModel(
    val type: SignalDataModelTypes?=null,
    val data: String?=null
)

enum class SignalDataModelTypes{
    OFFER,
    ANSWER,
    ICE,
    CHAT
}