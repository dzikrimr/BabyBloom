package com.example.bubtrack.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bubtrack.ui.camera.CameraPreview
import com.example.bubtrack.ui.components.OverallStatusCard
import com.example.bubtrack.ui.components.StatusCard
import com.example.bubtrack.viewmodel.SleepDetectionViewModel

@Composable
fun BabySleepMonitorScreen(
    viewModel: SleepDetectionViewModel,
    onNavigateToVoiceAnalyzer: () -> Unit
) {
    val sleepStatus by viewModel.sleepStatus.collectAsState()

    LaunchedEffect(sleepStatus) {
        Log.d("BabySleepMonitorScreen", "SleepStatus updated: $sleepStatus")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CameraPreview(
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        OverallStatusCard(
            status = sleepStatus.overallStatus,
            modifier = Modifier.fillMaxWidth()
        )

        StatusCard(
            title = "Status Bayi",
            status = sleepStatus,
            modifier = Modifier.fillMaxWidth()
        )

        // Tombol navigasi ke Voice Analyzer
        Button(
            onClick = onNavigateToVoiceAnalyzer,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Voice Analyzer",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Analisis Suara Bayi",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}