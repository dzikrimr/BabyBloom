package com.example.bubtrack.presentation.ai.sleepmonitor

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
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
import androidx.navigation.compose.rememberNavController
import com.example.bubtrack.R
import com.example.bubtrack.data.webrtc.ConnectionState
import com.example.bubtrack.data.webrtc.WebRTCService
import com.example.bubtrack.presentation.ai.sleepmonitor.comps.DevicePairingPopup
import com.example.bubtrack.ui.theme.AppBackground
import kotlinx.coroutines.delay
import org.webrtc.SurfaceViewRenderer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalGetImage::class)
@Composable
fun SleepMonitorScreen(
    navController: NavController,
    sleepViewModel: SleepMonitorViewModel,
    webRTCService: WebRTCService,
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
    val localRenderer = remember { SurfaceViewRenderer(context) }
    val remoteRenderer = remember { SurfaceViewRenderer(context) }

    val connectionState by webRTCService.connectionState.collectAsState()
    val isHost by webRTCService.isHost.collectAsState()
    val isProcessing by sleepViewModel.isProcessing.collectAsState()

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

    // Initialize WebRTC renderers
    LaunchedEffect(connectionState) {
        if (connectionState == ConnectionState.CONNECTED) {
            webRTCService.localRenderer.value?.let { renderer ->
                webRTCService.eglBase?.eglBaseContext?.let { context ->
                    renderer.init(context, null)
                }
            }
            webRTCService.remoteRenderer.value?.let { renderer ->
                webRTCService.eglBase?.eglBaseContext?.let { context ->
                    renderer.init(context, null)
                }
            }
        }
    }

    // Check camera permission and initialize camera executor
    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    // Start camera and analysis when permission is granted
    LaunchedEffect(hasCameraPermission, isHost) {
        if (hasCameraPermission && cameraExecutor != null && isHost) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val provider = cameraProviderFuture.get()
                cameraProvider = provider

                val newPreview = Preview.Builder()
                    .setTargetResolution(Size(640, 480))
                    .build()
                preview = newPreview

                val newImageAnalyzer = ImageAnalysis.Builder()
                    .setTargetResolution(Size(640, 480))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor!!) { imageProxy ->
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastProcessedTime >= 1000) {
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    Log.d("SleepMonitorScreen", "Processing frame: width=${imageProxy.width}, height=${imageProxy.height}, rotation=${imageProxy.imageInfo.rotationDegrees}")
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
                    if (isPreviewVisible) {
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
                    text = "Baby Monitor AI",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.width(25.dp))
            }
        }

        // Timer Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFA78BFA))
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Monitoring Time",
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = monitoringTime,
                    color = Color.White,
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
                .background(Color.Gray.copy(alpha = 0.3f))
        ) {
            if (hasCameraPermission && isPreviewVisible) {
                if (isHost) {
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AndroidView(
                        factory = { remoteRenderer },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else if (!hasCameraPermission) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Camera Permission Required",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Camera Preview Hidden",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
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
                        text = when (connectionState) {
                            ConnectionState.CONNECTED -> if (isHost) "Live AI Detection" else "Remote Feed"
                            ConnectionState.CONNECTING -> "Connecting..."
                            ConnectionState.FAILED -> "Connection Failed"
                            else -> if (hasCameraPermission) "Live AI Detection" else "Camera Off"
                        },
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isHost) "$deviceName Camera" else "Remote Device",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Camera preview toggle button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
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
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Status Cards Grid
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
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusCard(
                    modifier = Modifier.weight(1f),
                    title = "Suara",
                    value = if (isProcessing) "Nangis" else "Tidak",
                    painter = painterResource(R.drawable.ic_speaker),
                    backgroundColor = Color(0xFFE8F5E9),
                    iconColor = Color(0xFF4CAF50),
                    valueColor = if (isProcessing) Color.Red else Color(0xFF4CAF50)
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
                    webRTCService.cleanup()
                    onStopMonitor()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5FBF)
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
                    text = "Stop AI Monitor",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
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
                        containerColor = Color(0xFF64B5F6)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_mic),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Cry Mode",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                Button(
                    onClick = { showPairingPopup = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF48FB1)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_qr_scan),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pair Device",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }

    // Device Pairing Popup
    if (showPairingPopup) {
        DevicePairingPopup(
            onDismiss = { showPairingPopup = false },
            webRTCService = webRTCService,
            localRenderer = localRenderer,
            remoteRenderer = remoteRenderer,
            onPairingSuccess = { sessionId ->
                if (!isHost) {
                    webRTCService.joinAsClient(sessionId, localRenderer, remoteRenderer)
                }
            }
        )
    }

    // Clean up camera and WebRTC when composable is disposed
    DisposableEffect(lifecycleOwner) {
        onDispose {
            cameraProvider?.unbindAll()
            cameraExecutor?.shutdown()
            localRenderer.release()
            remoteRenderer.release()
            webRTCService.cleanup()
        }
    }
}

@Composable
private fun StatusCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    painter: Painter,
    backgroundColor: Color,
    iconColor: Color,
    valueColor: Color
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
                    tint = Color.White
                )
            }
            Text(
                text = title,
                fontSize = 13.sp,
                color = Color.Gray,
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
