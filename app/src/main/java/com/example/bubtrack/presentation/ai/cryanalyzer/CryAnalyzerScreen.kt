package com.example.bubtrack.presentation.ai.cryanalyzer

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.bubtrack.R
import com.example.bubtrack.presentation.ai.comps.BabyNeedPager
import com.example.bubtrack.ui.theme.AppBackground
import com.example.bubtrack.ui.theme.AppPurple
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryAnalyzerScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    viewModel: CryAnalyzerViewModel // Added to match MainNavigation
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

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
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    // Timer for recording duration
    var seconds by remember { mutableStateOf(0) }
    LaunchedEffect(state.isRecording) {
        if (state.isRecording) {
            seconds = 0 // Reset timer when starting recording
            while (state.isRecording) {
                delay(1000L)
                seconds++
            }
        }
    }
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, remainingSeconds)

    // UI from new code
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { onNavigateBack() },
                modifier = modifier.width(25.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "back button",
                    modifier = modifier.fillMaxSize()
                )
            }
            Text(
                "Cry Analyzer",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Spacer(modifier = modifier.width(25.dp))
        }
        Spacer(modifier = Modifier.height(32.dp))
        Column(
            modifier = modifier
                .padding(16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        color = if (state.isRecording) Color(0xFFF87171) else Color.Gray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(percent = 50)
                    )
                    .scale(if (state.isRecording) scale else 1f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_speaker),
                    contentDescription = "record button",
                    modifier = Modifier.size(50.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFEF2F2))
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Cry Duration",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    formattedTime,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Column(
            modifier = modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(12.dp)
        ) {
            Text(
                "Baby's need analysis",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(12.dp))
            BabyNeedPager(
                results = state.confidenceScores
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                } else {
                    viewModel.onEvent(VoiceAnalyzerEvent.ToggleRecording)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = AppPurple
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = modifier.fillMaxWidth().height(50.dp).padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(
                        id = if (state.isRecording) R.drawable.ic_muted else R.drawable.ic_mic
                    ),
                    contentDescription = "mic button"
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (state.isRecording) "Stop Recording" else "Start Recording",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}