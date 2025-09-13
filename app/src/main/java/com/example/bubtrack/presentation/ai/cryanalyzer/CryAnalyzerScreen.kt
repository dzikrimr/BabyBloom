package com.example.bubtrack.presentation.ai.cryanalyzer

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryAnalyzerScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: CryAnalyzerViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            viewModel.onEvent(VoiceAnalyzerEvent.PermissionDenied)
        }
    }

    // Animation for recording state
    val infiniteTransition = rememberInfiniteTransition(label = "recording_animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Button(onClick = onNavigateBack) { Text("← Kembali") }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Analisis Tangisan Bayi",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(110.dp)
                .background(
                    color = if (state.isRecording) Color.Red.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(percent = 50)
                )
                .scale(if (state.isRecording) scale else 1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (state.isRecording) "Merekam" else "Siap",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                } else {
                    viewModel.onEvent(VoiceAnalyzerEvent.ToggleRecording)
                }
            },
            modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = if (state.isRecording) "Berhenti Rekam" else "Mulai Rekam", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = state.classificationResult, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                if (state.confidenceScores.isNotEmpty()) {
                    Text("Skor Kepercayaan:", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    state.confidenceScores.forEach { (label, score) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(label)
                            Text(String.format("%.2f%%", score * 100))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))
    }
}