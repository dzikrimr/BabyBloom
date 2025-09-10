package com.example.bubtrack.data.models

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlin.math.*

data class BabyFeatures(
    val eyeAspectRatio: Float = 0f,
    val movement: Float = 0f,
    val isRollover: Boolean = false
)