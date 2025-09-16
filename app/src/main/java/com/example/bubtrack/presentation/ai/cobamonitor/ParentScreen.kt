package com.example.bubtrack.presentation.ai.cobamonitor

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
fun ParentScreen(
    viewModel: MonitorRoomViewModel = hiltViewModel()
) {
    var roomId by remember { mutableStateOf<String?>(null) }
    var isMonitoring by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isMonitoring) {
            Text(
                text = "Parent Device",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    viewModel.createRoom { generatedId ->
                        roomId = generatedId
                        isMonitoring = true
                    }
                }
            ) {
                Text("Create Room & Start Monitoring")
            }

            if (roomId != null) {
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "Room ID: $roomId",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                roomId?.let {
                    Text(
                        text = "Room ID: $it",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Button(
                    onClick = {
                        isMonitoring = false
                        roomId?.let { id ->
                            viewModel.stopMonitoring(id) // 🚨 trigger ke baby untuk mulai pose analysis
                        }
                    }
                ) {
                    Text("Stop Monitoring")
                }

                SurfaceViewRendererComposable(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    onSurfaceReady = { renderer: SurfaceViewRenderer ->
                        Log.d("ParentScreen", "Surface ready, initializing remote renderer")
                        viewModel.initRemoteSurface(renderer)
                    },
                    message = null
                )

                Spacer(Modifier.height(16.dp))


            }
        }
    }
}
