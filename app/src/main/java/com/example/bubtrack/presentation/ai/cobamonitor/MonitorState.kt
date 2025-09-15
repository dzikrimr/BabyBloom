package com.example.bubtrack.presentation.ai.cobamonitor

sealed class MonitorState {
    object Idle : MonitorState()
    object CreatingRoom : MonitorState()
    object WaitingForBaby : MonitorState()
    object Connecting : MonitorState()
    object Connected : MonitorState()
    data class Error(val message: String) : MonitorState()
}