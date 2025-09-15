package com.example.bubtrack.presentation.ai.cobamonitor

sealed class MonitorState {
    object Idle : MonitorState()
    object WaitingForBaby : MonitorState()
    object Connecting : MonitorState()
    object Connected : MonitorState()
    object AnalyzingPose : MonitorState()
    data class Error(val msg: String) : MonitorState()
}
