package com.example.bubtrack.presentation.ai.sleepmonitor

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.SurfaceView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.bubtrack.R
import com.example.bubtrack.data.livekit.LiveKitConnectionState
import com.example.bubtrack.presentation.ai.sleepmonitor.comps.DevicePairingPopup
import com.example.bubtrack.ui.theme.AppBackground
import io.livekit.android.renderer.SurfaceViewRenderer
import kotlinx.coroutines.delay
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalGetImage::class)
@Composable
fun SleepMonitorScreen(
    navController: NavController,
    sleepViewModel: SleepMonitorViewModel,
    onBackClick: () -> Unit = {},
    onStopMonitor: () -> Unit = {},
    onCryModeClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var monitoringTime by remember { mutableStateOf("00:00:00") }
    var showPairingPopup by remember { mutableStateOf(false) }
    var isPreviewVisible by remember { mutableStateOf(true) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
    var cameraExecutor: ExecutorService? by remember { mutableStateOf(null) }
    val previewView = remember { PreviewView(context).apply { implementationMode = PreviewView.ImplementationMode.COMPATIBLE } }
    var preview by remember { mutableStateOf<Preview?>(null) }
    var imageAnalyzer by remember { mutableStateOf<ImageAnalysis?>(null) }
    var isInitialized by remember { mutableStateOf(false) }

    // Collect states from ViewModel (including LiveKit states)
    val sleepStatus by sleepViewModel.sleepStatus.collectAsState()
    val isProcessing by sleepViewModel.isProcessing.collectAsState()
    val connectionState by sleepViewModel.connectionState.collectAsState()
    val participantCount by sleepViewModel.participantCount.collectAsState()
    val remoteBabyData by sleepViewModel.remoteBabyData.collectAsState()
    val isHost by sleepViewModel.isHost.collectAsState()
    val remoteVideoTrack by sleepViewModel.getRemoteVideoTrack().collectAsState()

    // Get device name
    val deviceName = remember {
        "${Build.BRAND} ${Build.MODEL}".let { name ->
            if (name.length > 20) name.take(17) + "..." else name
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Initialize as host and setup camera when screen opens
    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Initialize as host and connect immediately
        if (!isInitialized) {
            sleepViewModel.initializeAsHost()
            isInitialized = true
        }
    }

    // Start camera and analysis when permission is granted AND user is host
    LaunchedEffect(hasCameraPermission, isHost, connectionState) {
        if (hasCameraPermission && cameraExecutor != null && isHost) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val provider = cameraProviderFuture.get()
                cameraProvider = provider

                val newPreview = Preview.Builder()
                    .setTargetResolution(android.util.Size(640, 480))
                    .build()
                preview = newPreview

                val newImageAnalyzer = ImageAnalysis.Builder()
                    .setTargetResolution(android.util.Size(640, 480))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor!!) { imageProxy ->
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastProcessedTime >= 1000) { // Throttle to ~1 FPS
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    Log.d("SleepMonitorScreen", "Processing frame: width=${imageProxy.width}, height=${imageProxy.height}, rotation=${imageProxy.imageInfo.rotationDegrees}")
                                    sleepViewModel.processFrame(imageProxy)
                                    lastProcessedTime = currentTime
                                } else {
                                    Log.e("SleepMonitorScreen", "No media image available")
                                    imageProxy.close()
                                }
                            } else {
                                imageProxy.close()
                                Log.d("SleepMonitorScreen", "Frame skipped due to throttling")
                            }
                        }
                    }
                imageAnalyzer = newImageAnalyzer

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    provider.unbindAll()
                    if (isPreviewVisible && isHost) { // Only show local preview if host
                        newPreview.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        newPreview,
                        newImageAnalyzer
                    )
                    Log.d("SleepMonitorScreen", "Camera bound successfully")
                } catch (exc: Exception) {
                    Log.e("SleepMonitorScreen", "Camera binding failed: ${exc.message}", exc)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    // Update preview surface when visibility changes
    LaunchedEffect(isPreviewVisible, isHost) {
        if (hasCameraPermission && preview != null && isHost) {
            if (isPreviewVisible) {
                preview?.setSurfaceProvider(previewView.surfaceProvider)
            } else {
                preview?.setSurfaceProvider(null)
            }
        }
    }

    // Timer logic
    LaunchedEffect(Unit) {
        var startTime = System.currentTimeMillis()
        while (true) {
            delay(1000)
            val elapsedTime = (System.currentTimeMillis() - startTime) / 1000
            val hours = elapsedTime / 3600
            val minutes = (elapsedTime % 3600) / 60
            val seconds = elapsedTime % 60
            monitoringTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(androidx.compose.ui.graphics.Color.White)
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
                    text = "Baby Monitor AI",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.width(25.dp))
            }

            // LiveKit Connection Status with Role Info
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        when (connectionState) {
                            LiveKitConnectionState.CONNECTED -> Color(0xFFE7F7E7)
                            LiveKitConnectionState.CONNECTING -> Color(0xFFFFF4E6)
                            LiveKitConnectionState.FAILED -> Color(0xFFFFE6E6)
                            LiveKitConnectionState.DISCONNECTED -> Color(0xFFF0F0F0)
                            LiveKitConnectionState.RECONNECTING -> TODO()
                        },
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (connectionState) {
                        LiveKitConnectionState.CONNECTED -> {
                            val roleText = if (isHost) "HOST" else "VIEWER"
                            val statusText = if (participantCount > 1) "Connected" else "Ready for pairing"
                            "🔗 $statusText • $participantCount device(s) • $roleText"
                        }
                        LiveKitConnectionState.CONNECTING -> "🔄 Connecting..."
                        LiveKitConnectionState.RECONNECTING -> "🔄 Reconnecting..."
                        LiveKitConnectionState.FAILED -> "❌ Connection failed"
                        LiveKitConnectionState.DISCONNECTED -> {
                            val roleText = if (isHost) "HOST" else "VIEWER"
                            "📱 Ready • $roleText • Tap 'Pair Device' to share"
                        }
                    },
                    fontSize = 12.sp,
                    color = when (connectionState) {
                        LiveKitConnectionState.CONNECTED -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                        LiveKitConnectionState.CONNECTING, LiveKitConnectionState.RECONNECTING -> androidx.compose.ui.graphics.Color(0xFFFF9800)
                        LiveKitConnectionState.FAILED -> androidx.compose.ui.graphics.Color(0xFFF44336)
                        LiveKitConnectionState.DISCONNECTED -> androidx.compose.ui.graphics.Color(0xFF666666)
                    }
                )
            }
        }

        // Timer Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(androidx.compose.ui.graphics.Color(0xFFA78BFA))
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Monitoring Time",
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = monitoringTime,
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Camera Feed Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.3f))
        ) {
            when {
                // Show remote video feed if viewer and remote track available
                !isHost && remoteVideoTrack != null -> {
                    RemoteVideoView(
                        videoTrack = remoteVideoTrack!!,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                // Show local camera preview if host and has permission
                isHost && hasCameraPermission && isPreviewVisible -> {
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                // Show placeholders for various states
                !hasCameraPermission -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Camera Permission Required",
                            color = androidx.compose.ui.graphics.Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
                !isHost && remoteVideoTrack == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (connectionState == LiveKitConnectionState.CONNECTED)
                                    "Waiting for Host Video" else "Not Connected to Host",
                                color = androidx.compose.ui.graphics.Color.Gray,
                                fontSize = 14.sp
                            )
                            if (connectionState == LiveKitConnectionState.CONNECTED) {
                                Spacer(modifier = Modifier.height(8.dp))
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = androidx.compose.ui.graphics.Color(0xFFA78BFA)
                                )
                            }
                        }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Camera Preview Hidden",
                            color = androidx.compose.ui.graphics.Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Camera info overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = when {
                            !isHost && remoteVideoTrack != null -> "Remote Live Feed"
                            isHost && hasCameraPermission -> "Live AI Detection"
                            else -> "Camera Off"
                        },
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (!isHost) "Host Device Camera" else "$deviceName Camera",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Camera preview toggle button (only for host)
            if (isHost) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(40.dp)
                        .background(
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f),
                            CircleShape
                        )
                        .clickable {
                            if (hasCameraPermission) {
                                isPreviewVisible = !isPreviewVisible
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            if (isPreviewVisible && hasCameraPermission) R.drawable.ic_eye else R.drawable.ic_eye_off
                        ),
                        contentDescription = if (isPreviewVisible && hasCameraPermission) "Hide Preview" else "Show Preview",
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Status Cards Grid - Show remote data if available (for viewer) or local data (for host)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusCard(
                    modifier = Modifier.weight(1f),
                    title = "Sleep Status",
                    value = if (!isHost && remoteBabyData != null) {
                        // Viewer shows remote data
                        when {
                            remoteBabyData!!.eyeStatus.contains("Tertutup") -> "Asleep (Remote)"
                            remoteBabyData!!.eyeStatus.contains("Terbuka") -> "Awake (Remote)"
                            else -> "Unknown (Remote)"
                        }
                    } else {
                        // Host shows local data
                        when {
                            sleepStatus.eyeStatus.contains("Tertutup") -> "Asleep"
                            sleepStatus.eyeStatus.contains("Terbuka") -> "Awake"
                            else -> "Unknown"
                        }
                    },
                    painter = painterResource(R.drawable.ic_eye_off),
                    backgroundColor = androidx.compose.ui.graphics.Color(0xFFE3F2FD),
                    iconColor = androidx.compose.ui.graphics.Color(0xFFF9A8D4),
                    valueColor = androidx.compose.ui.graphics.Color(0xFFF9A8D4)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusCard(
                    modifier = Modifier.weight(1f),
                    title = "Sound",
                    value = if (isProcessing) "Crying" else "Normal",
                    painter = painterResource(R.drawable.ic_speaker),
                    backgroundColor = androidx.compose.ui.graphics.Color(0xFFE8F5E9),
                    iconColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                    valueColor = if (isProcessing) androidx.compose.ui.graphics.Color.Red else androidx.compose.ui.graphics.Color(0xFF4CAF50)
                )
                StatusCard(
                    modifier = Modifier.weight(1f),
                    title = "Rollover",
                    value = if (!isHost && remoteBabyData != null) {
                        // Viewer shows remote data
                        when {
                            remoteBabyData!!.rolloverStatus.contains("Ya") -> "Detected (Remote)"
                            else -> "Normal (Remote)"
                        }
                    } else {
                        // Host shows local data
                        when {
                            sleepStatus.rolloverStatus.contains("Ya") -> "Detected"
                            else -> "Normal"
                        }
                    },
                    painter = painterResource(R.drawable.ic_rollover),
                    backgroundColor = androidx.compose.ui.graphics.Color(0xFFF3E5F5),
                    iconColor = androidx.compose.ui.graphics.Color(0xFFA78BFA),
                    valueColor = if ((if (!isHost && remoteBabyData != null) remoteBabyData!!.rolloverStatus else sleepStatus.rolloverStatus).contains("Ya"))
                        androidx.compose.ui.graphics.Color.Red else androidx.compose.ui.graphics.Color(0xFFA78BFA)
                )
            }
        }

        // Action Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    cameraProvider?.unbindAll()
                    sleepViewModel.resetSession()
                    sleepViewModel.disconnectFromRoom() // Disconnect from LiveKit room
                    onStopMonitor()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF8B5FBF)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_stop),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = androidx.compose.ui.graphics.Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Stop AI Monitor",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onCryModeClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFF64B5F6)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_mic),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = androidx.compose.ui.graphics.Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Cry Mode",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }

                Button(
                    onClick = { showPairingPopup = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFFF48FB1)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_qr_scan),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = androidx.compose.ui.graphics.Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pair Device",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }
            }

            // LiveKit Control Buttons (if connected)
            if (connectionState == LiveKitConnectionState.CONNECTED) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Only show camera controls for host
                    if (isHost) {
                        Button(
                            onClick = { sleepViewModel.switchCamera() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = androidx.compose.ui.graphics.Color(0xFF4CAF50)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_camera),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = androidx.compose.ui.graphics.Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Switch", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.White)
                        }

                        Button(
                            onClick = { sleepViewModel.toggleVideo() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = androidx.compose.ui.graphics.Color(0xFF2196F3)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_eye),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = androidx.compose.ui.graphics.Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Video", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.White)
                        }
                    }

                    Button(
                        onClick = { sleepViewModel.toggleAudio() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.ui.graphics.Color(0xFFFF9800)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_mic),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Audio", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }

    // Device Pairing Popup
    if (showPairingPopup) {
        DevicePairingPopup(
            onDismiss = { showPairingPopup = false },
            sleepViewModel = sleepViewModel,
            onPairingSuccess = {
                Log.d("SleepMonitorScreen", "Pairing successful")
                showPairingPopup = false
            }
        )
    }

    // Clean up camera when composable is disposed
    DisposableEffect(lifecycleOwner) {
        onDispose {
            cameraProvider?.unbindAll()
            cameraExecutor?.shutdown()
        }
    }
}

@Composable
private fun RemoteVideoView(
    videoTrack: io.livekit.android.room.track.VideoTrack,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            SurfaceViewRenderer(context).apply {
                init(null, null)
                videoTrack.addRenderer(this)
            }
        },
        modifier = modifier,
        onRelease = { surfaceViewRenderer ->
            videoTrack.removeRenderer(surfaceViewRenderer)
            surfaceViewRenderer.release()
        }
    )
}

@Composable
private fun StatusCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    painter: Painter,
    backgroundColor: androidx.compose.ui.graphics.Color,
    iconColor: androidx.compose.ui.graphics.Color,
    valueColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }
            Text(
                text = title,
                fontSize = 13.sp,
                color = androidx.compose.ui.graphics.Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

private var lastProcessedTime = 0L