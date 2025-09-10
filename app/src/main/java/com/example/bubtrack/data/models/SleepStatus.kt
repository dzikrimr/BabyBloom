package com.example.bubtrack.data.models

data class SleepStatus(
    val eyeStatus: String = "Tidak terdeteksi",
    val movementStatus: String = "Tidak terdeteksi",
    val rolloverStatus: String = "Tidak terdeteksi",
    val overallStatus: String = "Monitoring..."
)