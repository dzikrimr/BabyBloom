package com.example.bubtrack.presentation.ai.monitor

import android.util.Log
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bubtrack.R
import org.webrtc.SurfaceViewRenderer
import java.text.SimpleDateFormat
import java.util.*

data class MotionLogEntry(
    val timestamp: String,
    val motionType: String,
    val description: String
)

@Composable
fun BabyScreen(
    viewModel: MonitorRoomViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    var roomId by remember { mutableStateOf("") }
    var isStreaming by remember { mutableStateOf(false) }
    val isAnalyzingPose by viewModel.isAnalyzingPose.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var previewView: PreviewView? by remember { mutableStateOf(null) }

    // Motion detection logs
    var motionLogs by remember { mutableStateOf<List<MotionLogEntry>>(emptyList()) }
    val listState = rememberLazyListState()

    LaunchedEffect(isAnalyzingPose) {
        if (isAnalyzingPose) {
            previewView?.let {
                viewModel.startPoseAnalysis(context, lifecycleOwner, it)
            }
        }
    }

    // Add motion log when pose state changes
    val poseState by viewModel.poseState.collectAsState()
    LaunchedEffect(poseState) {
        if (poseState.isNotEmpty() && isAnalyzingPose) {
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val newEntry = MotionLogEntry(
                timestamp = timeFormat.format(Date()),
                motionType = "Pose Detection",
                description = "Current pose: $poseState"
            )
            motionLogs = (motionLogs + newEntry).takeLast(20)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header with Back Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.width(25.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.fillMaxSize(),
                        tint = Color(0xFF2D3748)
                    )
                }
                Text(
                    "Baby Device (Camera)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2D3748)
                )
                Spacer(modifier = Modifier.width(25.dp))
            }

            // Subtitle
            Text(
                text = when {
                    isAnalyzingPose -> "AI Motion Detection Active"
                    isStreaming -> "Streaming to Parent"
                    else -> "Ready to Connect"
                },
                fontSize = 14.sp,
                color = Color(0xFF718096),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }

        when {
            !isStreaming -> {
                // Connection Setup Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Icon
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    Color(0xFF60A5FA).copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_camera),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = Color(0xFF60A5FA)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Title
                        Text(
                            text = "Hubungkan ke Monitor",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3748),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Description
                        Text(
                            text = "Masukkan Room ID dari perangkat orang tua untuk mulai streaming kamera dengan deteksi gerakan AI.",
                            fontSize = 16.sp,
                            color = Color(0xFF718096),
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Room ID Input
                        OutlinedTextField(
                            value = roomId,
                            onValueChange = { roomId = it },
                            label = { Text("Room ID dari Perangkat Orang Tua") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF60A5FA),
                                focusedLabelColor = Color(0xFF60A5FA)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Connect Button
                        Button(
                            onClick = {
                                if (roomId.isNotBlank()) {
                                    isStreaming = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF60A5FA)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            enabled = roomId.isNotBlank()
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_play),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Mulai Stream Kamera",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            isAnalyzingPose -> {
                // Motion Detection Mode
                // Camera Feed Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AndroidView(
                            factory = { ctx ->
                                PreviewView(ctx).apply {
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                }.also { previewView = it }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp)),
                            update = { pv -> previewView = pv }
                        )

                        // AI Status Overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                                .background(
                                    Color(0xFF10B981).copy(alpha = 0.9f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color.White, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AI Active",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Current Pose Display
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.7f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Motion Detection",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (poseState.isNotEmpty()) poseState else "Analyzing...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFFBBF24),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            else -> {
                // Normal streaming
                SurfaceViewRendererComposable(
                    modifier = Modifier.fillMaxSize(),
                    onSurfaceReady = { renderer: SurfaceViewRenderer ->
                        Log.d("BabyScreen", "Surface ready, starting local stream")
                        viewModel.joinRoomAsBaby(roomId, renderer)
                    },
                    message = null
                )
            }
        }
    }
}

