package com.example.bubtrack.presentation.ai.sleepmonitor.comps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bubtrack.R
import com.example.bubtrack.models.SleepStatus
import java.text.SimpleDateFormat
import java.util.*

data class LogEntryData(
    val timestamp: String,
    val sleepStatus: String,
    val motion: String,
    val rollover: String
)

@Composable
fun MonitorLogCard(
    modifier: Modifier = Modifier,
    isProcessing: Boolean,
    sleepStatus: SleepStatus? = null,
    logEntries: List<LogEntryData> = emptyList()
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Real-time Monitor Log",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (isProcessing) Color(0xFF10B981) else Color(0xFFEF4444),
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isProcessing) "Active" else "Inactive",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Log entries with scroll
            if (logEntries.isEmpty()) {
                // Show sample data or empty state
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    reverseLayout = true
                ) {
                    items(10) { index ->
                        val currentTime = System.currentTimeMillis() - (index * 5000)
                        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

                        LogEntryItem(
                            entry = LogEntryData(
                                timestamp = timeFormat.format(Date(currentTime)),
                                sleepStatus = sleepStatus?.let {
                                    when {
                                        it.eyeStatus.contains("Tertutup") -> "Asleep"
                                        it.eyeStatus.contains("Terbuka") -> "Awake"
                                        else -> "Unknown"
                                    }
                                } ?: "Unknown",
                                motion = sleepStatus?.let {
                                    when {
                                        it.movementStatus.contains("aktif") -> "Active"
                                        else -> "Still"
                                    }
                                } ?: "Unknown",
                                rollover = sleepStatus?.let {
                                    when {
                                        it.rolloverStatus.contains("Ya") -> "Detected"
                                        else -> "Normal"
                                    }
                                } ?: "Unknown"
                            )
                        )
                    }
                }
            } else {
                // Show actual log data
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    reverseLayout = true
                ) {
                    items(logEntries) { entry ->
                        LogEntryItem(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogEntryItem(
    entry: LogEntryData
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC)
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Timestamp
            Text(
                text = entry.timestamp,
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Status indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Sleep Status
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(
                            if (entry.sleepStatus == "Asleep") R.drawable.ic_eye_off else R.drawable.ic_eye
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = when (entry.sleepStatus) {
                            "Asleep" -> Color(0xFF3B82F6)
                            "Awake" -> Color(0xFF10B981)
                            else -> Color(0xFF6B7280)
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = entry.sleepStatus,
                        fontSize = 12.sp,
                        color = when (entry.sleepStatus) {
                            "Asleep" -> Color(0xFF3B82F6)
                            "Awake" -> Color(0xFF10B981)
                            else -> Color(0xFF6B7280)
                        },
                        fontWeight = FontWeight.Medium
                    )
                }

                // Motion
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_motion),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (entry.motion == "Active") Color(0xFFF59E0B) else Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = entry.motion,
                        fontSize = 12.sp,
                        color = if (entry.motion == "Active") Color(0xFFF59E0B) else Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Rollover
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_rollover),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (entry.rollover == "Detected") Color(0xFFEF4444) else Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = entry.rollover,
                        fontSize = 12.sp,
                        color = if (entry.rollover == "Detected") Color(0xFFEF4444) else Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}