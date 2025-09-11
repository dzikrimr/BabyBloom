package com.example.bubtrack.presentation.ai.sleepmonitor

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
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
import androidx.navigation.compose.rememberNavController
import com.example.bubtrack.R
import com.example.bubtrack.presentation.ai.sleepmonitor.comps.DevicePairingPopup
import com.example.bubtrack.ui.theme.AppBackground
import kotlinx.coroutines.delay

@Composable
fun SleepMonitorScreen(
    navController: NavController,
    onBackClick: () -> Unit = {},
    onStopMonitor: () -> Unit = {},
    onCryModeClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var monitoringTime by remember { mutableStateOf("02:34:14") }
    var showPairingPopup by remember { mutableStateOf(false) }
    var isPreviewVisible by remember { mutableStateOf(true) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
    val previewView = remember { PreviewView(context).apply { implementationMode = PreviewView.ImplementationMode.COMPATIBLE } }
    var preview by remember { mutableStateOf<Preview?>(null) }

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

    // Check camera permission and start camera on first launch
    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Start camera when permission is granted
    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val provider = cameraProviderFuture.get()
                cameraProvider = provider

                val newPreview = Preview.Builder().build()
                preview = newPreview
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    provider.unbindAll()
                    if (isPreviewVisible) {
                        newPreview.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        newPreview
                    )
                } catch (exc: Exception) {
                    // Handle camera binding error
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    // Update preview surface when visibility changes
    LaunchedEffect(isPreviewVisible) {
        if (hasCameraPermission && preview != null) {
            if (isPreviewVisible) {
                preview?.setSurfaceProvider(previewView.surfaceProvider)
            } else {
                preview?.setSurfaceProvider(null)
            }
        }
    }

    // Simulate timer updates
    LaunchedEffect(Unit) {
        var seconds = 14
        var minutes = 34
        var hours = 2
        while (true) {
            delay(1000)
            seconds++
            if (seconds >= 60) {
                seconds = 0
                minutes++
                if (minutes >= 60) {
                    minutes = 0
                    hours++
                }
            }
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
                    text = "Baby Monitor",
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
                // Camera Preview
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (!hasCameraPermission) {
                // Placeholder when no camera permission
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
                // Placeholder when preview is hidden
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
                        text = if (hasCameraPermission) "Live Stream" else "Camera Off",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$deviceName Camera",
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusCard(
                modifier = Modifier.weight(1f),
                title = "Sleep Status",
                value = "Asleep",
                painter = painterResource(R.drawable.ic_eye_off),
                backgroundColor = Color(0xFFE3F2FD),
                iconColor = Color(0xFF93C5FD),
                valueColor = Color(0xFF93C5FD)
            )
            StatusCard(
                modifier = Modifier.weight(1f),
                title = "Motion",
                value = "Detected",
                painter = painterResource(R.drawable.ic_motion),
                backgroundColor = Color(0xFFFCE4EC),
                iconColor = Color(0xFFF9A8D4),
                valueColor = Color(0xFFF9A8D4)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusCard(
                modifier = Modifier.weight(1f),
                title = "Rollover",
                value = "Normal",
                painter = painterResource(R.drawable.ic_rollover),
                backgroundColor = Color(0xFFF3E5F5),
                iconColor = Color(0xFFA78BFA),
                valueColor = Color(0xFFA78BFA)
            )
            StatusCard(
                modifier = Modifier.weight(1f),
                title = "Sound Level",
                value = "Quiet",
                painter = painterResource(R.drawable.ic_speaker),
                backgroundColor = Color(0xFFE8F5E8),
                iconColor = Color(0xFF4ADE80),
                valueColor = Color(0xFF16A34A)
            )
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
                    text = "Stop Monitor",
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
            onDismiss = { showPairingPopup = false }
        )
    }

    // Clean up camera when composable is disposed
    DisposableEffect(lifecycleOwner) {
        onDispose {
            cameraProvider?.unbindAll()
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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun SleepMonitorScreenPreview() {
    MaterialTheme {
        SleepMonitorScreen(navController = rememberNavController())
    }
}