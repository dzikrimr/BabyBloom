package com.example.bubtrack.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_sessions")
data class SleepSessionEntity(
    @PrimaryKey val id: Long = System.currentTimeMillis(),
    val startTime: Long,
    val endTime: Long? = null,
    val averageEAR: Float = 0f,
    val averageMAR: Float = 0f,
    val averageMovement: Float = 0f,
    val rolloverCount: Int = 0,
    val pacifierUsage: Boolean = false,
    val totalFramesProcessed: Int = 0
)