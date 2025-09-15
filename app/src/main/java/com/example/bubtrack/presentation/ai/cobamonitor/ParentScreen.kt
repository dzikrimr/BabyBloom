package com.example.bubtrack.presentation.ai.cobamonitor

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bubtrack.R
import com.example.bubtrack.presentation.ai.sleepmonitor.SleepMonitorViewModel
import com.example.bubtrack.presentation.ai.sleepmonitor.comps.MonitorLogCard
import com.example.bubtrack.presentation.ai.sleepmonitor.comps.LogEntryData
import kotlinx.coroutines.delay
import org.webrtc.SurfaceViewRenderer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

@OptIn(ExperimentalGetImage::class)
@Composable
fun ParentScreen(
    viewModel: MonitorRoomViewModel = hiltViewModel(),
    sleepViewModel: SleepMonitorViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var roomId by remember { mutableStateOf<String?>(null) }
    var isMonitoring by remember { mutableStateOf(false) }
    var monitoringTime by remember { mutableStateOf("00:00:00") }
    var isPreviewVisible by remember { mutableStateOf(true) }
    var showRoomIdCopied by remember { mutableStateOf(false) }
    var remoteSurfaceInitialized by remember { mutableStateOf(false) }
    var isAIProcessingEnabled by remember { mutableStateOf(true) }

    val state by viewModel.state.collectAsState()
    val sleepStatus by sleepViewModel.sleepStatus.collectAsState()
    val isProcessingSleep by sleepViewModel.isProcessing.collectAsState()

    // State for log entries
    var logEntries by remember { mutableStateOf<List<LogEntryData>>(emptyList()) }

    // Permission handler
    val permissionRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        Log.d("ParentScreen", "Permissions granted: $allGranted")
        if (!allGranted) {
            Toast.makeText(context, "Camera and Audio permissions required", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        permissionRequestLauncher.launch(
            arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO
            )
        )
    }

    // Timer logic
    LaunchedEffect(isMonitoring) {
        if (isMonitoring) {
            val startTime = System.currentTimeMillis()
            while (isMonitoring) {
                delay(1000)
                val elapsedTime = (System.currentTimeMillis() - startTime) / 1000
                val hours = elapsedTime / 3600
                val minutes = (elapsedTime % 3600) / 60
                val seconds = elapsedTime % 60
                monitoringTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }
        }
    }

    // Show copied message
    LaunchedEffect(showRoomIdCopied) {
        if (showRoomIdCopied) {
            delay(2000)
            showRoomIdCopied = false
        }
    }

    // Log entry generation based on sleep status - Updated with new status format
    LaunchedEffect(sleepStatus, isMonitoring, state) {
        if (isMonitoring && (state is MonitorState.Connected || isProcessingSleep)) {
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val newEntry = LogEntryData(
                timestamp = timeFormat.format(Date()),
                sleepStatus = when {
                    sleepStatus.eyeStatus.contains("Tertutup", ignoreCase = true) -> "Asleep"
                    sleepStatus.eyeStatus.contains("Terbuka", ignoreCase = true) -> "Awake"
                    sleepStatus.eyeStatus.contains("Setengah", ignoreCase = true) -> "Drowsy"
                    else -> "Unknown"
                },
                motion = when {
                    sleepStatus.movementStatus.contains("Bergerak aktif", ignoreCase = true) -> "Active"
                    else -> "Still"
                },
                rollover = when {
                    sleepStatus.rolloverStatus.contains("Ya", ignoreCase = true) -> "Detected"
                    else -> "Normal"
                }
            )

            // Add new entry and keep only last 50 entries
            logEntries = (logEntries + newEntry).takeLast(50)
        }
    }

    // Simulate AI processing when connected - Updated with better simulation data
    LaunchedEffect(state, isAIProcessingEnabled) {
        if (state is MonitorState.Connected && isAIProcessingEnabled) {
            // Start a coroutine that simulates processing frames
            while (state is MonitorState.Connected && isAIProcessingEnabled) {
                // Generate more realistic sleep detection simulation
                val currentTime = System.currentTimeMillis()
                val simulatedFeatures = generateRealisticSleepData(currentTime)

                // Update sleep status in ViewModel
                sleepViewModel.simulateProcessing(simulatedFeatures)

                delay(2000) // Process every 2 seconds
            }
        }
    }

    // Reset simulation when AI is disabled or monitoring stops
    LaunchedEffect(isAIProcessingEnabled, isMonitoring) {
        if (!isAIProcessingEnabled || !isMonitoring) {
            sleepViewModel.resetSimulation()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
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
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Text(
                    text = "Parent Device (Monitor)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.width(25.dp))
            }
        }

        // Video Feed Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isMonitoring) 300.dp else 0.dp)
                .padding(if (isMonitoring) 16.dp else 0.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isMonitoring) Color.Black else Color.Transparent)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    Log.d("ParentScreen", "Creating remote SurfaceViewRenderer in factory")
                    val renderer = SurfaceViewRenderer(ctx).apply {
                        setMirror(false)
                        setEnableHardwareScaler(true)
                    }

                    Log.d("ParentScreen", "Calling initRemoteSurface immediately")
                    viewModel.initRemoteSurface(renderer)
                    remoteSurfaceInitialized = true

                    renderer
                },
                update = { renderer ->
                    Log.d("ParentScreen", "Remote AndroidView update called")
                    if (!remoteSurfaceInitialized) {
                        Log.d("ParentScreen", "Late initRemoteSurface call")
                        viewModel.initRemoteSurface(renderer)
                        remoteSurfaceInitialized = true
                    }
                }
            )

            // Status overlays when monitoring
            if (isMonitoring) {
                when {
                    !isPreviewVisible -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Monitor Feed Hidden",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                    state is MonitorState.WaitingForBaby -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Blue.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color.White)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Waiting for Baby Device...",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Share Room ID: ${roomId ?: ""}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    state is MonitorState.Connecting -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Gray.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color.White)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Connecting to Baby...",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                    state is MonitorState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Red.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Error: ${(state as MonitorState.Error).message}",
                                color = Color.White,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    state is MonitorState.Connected -> {
                        // AI Processing indicator
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                                .background(
                                    if (isAIProcessingEnabled) Color.Green.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.8f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Text(
                                text = if (isAIProcessingEnabled) "✓ AI Active" else "AI Disabled",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Connection status
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                                .background(
                                    Color.Green.copy(alpha = 0.8f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "✓ Live Monitor",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Sleep detection overlay (new feature)
                        if (isAIProcessingEnabled && sleepStatus.eyeStatus.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.7f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = sleepStatus.eyeStatus,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = sleepStatus.movementStatus,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (sleepStatus.rolloverStatus.contains("Ya")) {
                                        Text(
                                            text = "⚠ ${sleepStatus.rolloverStatus}",
                                            color = Color.Red,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Monitor preview toggle
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(40.dp)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            CircleShape
                        )
                        .clickable { isPreviewVisible = !isPreviewVisible },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            if (isPreviewVisible) R.drawable.ic_eye else R.drawable.ic_eye_off
                        ),
                        contentDescription = if (isPreviewVisible) "Hide Monitor" else "Show Monitor",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (!isMonitoring) {
            // Setup screen when not monitoring
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFA78BFA))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Surface Ready: ${if(remoteSurfaceInitialized) "✓" else "✗"}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Preparing Monitor...",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Room Creation Section (existing code)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_eye),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF8B5FBF)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Start Baby Monitor",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF2D3748)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Create a monitoring room and share the Room ID with your baby device",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        if (roomId != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF0F9FF)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Room ID",
                                        fontSize = 14.sp,
                                        color = Color(0xFF64748B),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier
                                            .clickable {
                                                clipboardManager.setText(
                                                    AnnotatedString(
                                                        roomId ?: ""
                                                    )
                                                )
                                                showRoomIdCopied = true
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = roomId ?: "",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2563EB)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_copy),
                                            contentDescription = "Copy",
                                            modifier = Modifier.size(20.dp),
                                            tint = Color(0xFF2563EB)
                                        )
                                    }
                                    if (showRoomIdCopied) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Room ID copied!",
                                            fontSize = 12.sp,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Button(
                            onClick = {
                                if (roomId == null) {
                                    if (remoteSurfaceInitialized) {
                                        Log.d("ParentScreen", "Creating room with initialized surface")
                                        viewModel.createRoom { generatedId ->
                                            roomId = generatedId
                                        }
                                    } else {
                                        Toast.makeText(context, "Please wait for surface to initialize", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Log.d("ParentScreen", "Start monitoring clicked with roomId: $roomId")
                                    isMonitoring = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (roomId == null && !remoteSurfaceInitialized) Color.Gray else Color(0xFF8B5FBF)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = if (roomId == null) remoteSurfaceInitialized else true
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (roomId == null) R.drawable.ic_add else R.drawable.ic_play
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (roomId == null) {
                                    if (remoteSurfaceInitialized) "Create Room" else "Preparing..."
                                } else "Start Monitoring",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }

                        if (roomId != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Share this Room ID with your baby device to start monitoring",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        } else {
            // Monitoring active screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFA78BFA))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Monitoring Time",
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = monitoringTime,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Monitor Log Card - Updated condition
            MonitorLogCard(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                isProcessing = isAIProcessingEnabled && (state is MonitorState.Connected || isProcessingSleep),
                sleepStatus = sleepStatus,
                logEntries = logEntries
            )

            // Status Text - Updated to show more detailed info
            Text(
                text = "Room: ${roomId ?: "N/A"} | State: ${state::class.simpleName} | AI: ${if(isAIProcessingEnabled) "ON" else "OFF"} | Processing: ${if(isProcessingSleep) "YES" else "NO"}",
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            roomId?.let {
                                clipboardManager.setText(AnnotatedString(it))
                                showRoomIdCopied = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF64B5F6)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = roomId != null
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_copy),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Copy ID",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = {
                            isAIProcessingEnabled = !isAIProcessingEnabled
                            if (!isAIProcessingEnabled) {
                                // Reset simulation when disabled
                                sleepViewModel.resetSimulation()
                                logEntries = emptyList()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAIProcessingEnabled) Color(0xFF10B981) else Color(0xFFF59E0B)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_eye),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAIProcessingEnabled) "AI ON" else "AI OFF",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }

                Button(
                    onClick = {
                        Log.d("ParentScreen", "Stop monitoring clicked")
                        isMonitoring = false
                        isAIProcessingEnabled = false
                        roomId = null
                        logEntries = emptyList()
                        sleepViewModel.resetSimulation()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_stop),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Stop Monitoring",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// Updated helper function to generate more realistic sleep data
private fun generateRealisticSleepData(currentTime: Long): Map<String, Any> {
    // Create a more predictable seed based on time intervals
    val timeSegment = currentTime / 10000 // Changes every 10 seconds
    val random = Random(timeSegment)

    // Simulate a sleep progression over time
    val elapsedMinutes = (currentTime - (currentTime / 300000) * 300000) / 60000f // Reset every 5 minutes for demo

    return when {
        elapsedMinutes < 1f -> {
            // Awake phase
            mapOf(
                "eyeAspectRatio" to (random.nextFloat() * 0.3f + 0.7f), // 0.7-1.0 (awake)
                "movement" to if (random.nextFloat() < 0.7f) (random.nextFloat() * 0.6f + 0.4f) else 0f, // Often active
                "isRollover" to false,
                "isAwake" to true
            )
        }
        elapsedMinutes < 2.5f -> {
            // Getting sleepy
            mapOf(
                "eyeAspectRatio" to (random.nextFloat() * 0.4f + 0.3f), // 0.3-0.7 (drowsy)
                "movement" to if (random.nextFloat() < 0.4f) (random.nextFloat() * 0.5f + 0.2f) else 0f, // Less movement
                "isRollover" to false,
                "isAwake" to (random.nextFloat() < 0.3f)
            )
        }
        else -> {
            // Sleeping phase
            mapOf(
                "eyeAspectRatio" to (random.nextFloat() * 0.3f + 0.0f), // 0.0-0.3 (sleeping)
                "movement" to if (random.nextFloat() < 0.1f) (random.nextFloat() * 0.3f) else 0f, // Minimal movement
                "isRollover" to (random.nextFloat() < 0.05f), // 5% chance of rollover
                "isAwake" to false
            )
        }
    }
}