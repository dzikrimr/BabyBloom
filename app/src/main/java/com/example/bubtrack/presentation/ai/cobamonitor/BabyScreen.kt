package com.example.bubtrack.presentation.ai.cobamonitor

import android.util.Log
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.webrtc.SurfaceViewRenderer

@Composable
fun BabyScreen(
    viewModel: MonitorRoomViewModel = hiltViewModel()
) {
    var roomId by remember { mutableStateOf("") }
    var isStreaming by remember { mutableStateOf(false) }
    val isAnalyzingPose by viewModel.isAnalyzingPose.collectAsState() // 🔥 state dari VM
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var previewView: PreviewView? by remember { mutableStateOf(null) }

    LaunchedEffect(isAnalyzingPose) {
        if (isAnalyzingPose) {
            previewView?.let {
                viewModel.startPoseAnalysis(context, lifecycleOwner, it)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFEF2F2))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            !isStreaming -> {
                Text(
                    text = "Baby Device",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black
                )

                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = roomId,
                    onValueChange = { roomId = it },
                    label = { Text("Enter Room ID (from Parent)") }
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (roomId.isNotBlank()) {
                            isStreaming = true
                        }
                    }
                ) {
                    Text("Start Streaming")
                }
            }

            isAnalyzingPose -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp) // area kamera + overlay
                ) {
                    // Kamera
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }.also { previewView = it }
                        },
                        modifier = Modifier
                            .matchParentSize(),
                        update = { pv -> previewView = pv }
                    )

                    // Overlay hasil analisis
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .background(Color.Black.copy(alpha = 0.5f)) // biar teks terbaca
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Analyzing baby's pose...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Current Pose: ${viewModel.poseState.collectAsState().value}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Yellow
                        )
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

