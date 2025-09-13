package com.example.bubtrack.presentation.ai.cobamonitor

import com.example.bubtrack.presentation.ai.monitor.SurfaceViewRendererComposable
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.webrtc.SurfaceViewRenderer

@Composable
fun BabyScreen(
    viewModel: MonitorRoomViewModel = hiltViewModel()
) {
    var roomId by remember { mutableStateOf("") }
    var isStreaming by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFEF2F2))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isStreaming) {
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
        } else {
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
