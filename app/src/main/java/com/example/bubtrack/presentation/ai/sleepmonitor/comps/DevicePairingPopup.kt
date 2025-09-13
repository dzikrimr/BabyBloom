package com.example.bubtrack.presentation.ai.sleepmonitor.comps

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.bubtrack.R
import com.example.bubtrack.data.livekit.LiveKitConnectionState
import com.example.bubtrack.presentation.ai.sleepmonitor.SleepMonitorViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun DevicePairingPopup(
    onDismiss: () -> Unit,
    sleepViewModel: SleepMonitorViewModel,
    onPairingSuccess: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var selectedTab by remember { mutableStateOf("Show QR Code") }
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<String?>(null) }
    var showConnectionStatus by remember { mutableStateOf(false) }

    // Collect LiveKit states
    val connectionState by sleepViewModel.connectionState.collectAsState()
    val participantCount by sleepViewModel.participantCount.collectAsState()
    val isHost by sleepViewModel.isHost.collectAsState()

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Check camera permission
    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    // Generate QR code when showing QR tab - use existing room info from host
    LaunchedEffect(selectedTab) {
        if (selectedTab == "Show QR Code") {
            try {
                // If already a host, get the existing room info, otherwise generate new
                val roomInfo = sleepViewModel.generateRoomInfo()
                qrCodeBitmap = generateQRCode(roomInfo, 400)
                Log.d("DevicePairingPopup", "QR Code generated for room info: $roomInfo")
            } catch (e: Exception) {
                Log.e("DevicePairingPopup", "Failed to generate QR code: ${e.message}")
            }
        } else if (selectedTab == "Scan QR Code") {
            if (!hasCameraPermission) {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // Handle scan result - switch to viewer mode
    LaunchedEffect(scanResult) {
        scanResult?.let { result ->
            Log.d("DevicePairingPopup", "QR Code scanned: $result")
            showConnectionStatus = true
            try {
                val success = sleepViewModel.joinAsViewer(result)
                if (success) {
                    onPairingSuccess(result)
                } else {
                    Log.e("DevicePairingPopup", "Failed to join as viewer")
                }
            } catch (e: Exception) {
                Log.e("DevicePairingPopup", "Failed to join room: ${e.message}")
            }
        }
    }

    // Monitor connection state
    LaunchedEffect(connectionState) {
        when (connectionState) {
            LiveKitConnectionState.CONNECTED -> {
                showConnectionStatus = true
                if (participantCount > 1) { // Actually connected to another device
                    delay(2000) // Show success message for 2 seconds
                    onDismiss()
                }
            }
            LiveKitConnectionState.FAILED -> {
                showConnectionStatus = true
            }
            else -> {}
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "Device Pairing",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(0xFF1F2937),
                        modifier = Modifier.align(Alignment.Center)
                    )
                    IconButton(
                        onClick = {
                            onDismiss()
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .background(androidx.compose.ui.graphics.Color(0xFFF3F4F6), CircleShape)
                            .size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = androidx.compose.ui.graphics.Color(0xFF6B7280),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Connection Status
                if (showConnectionStatus || connectionState != LiveKitConnectionState.DISCONNECTED) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(
                                when (connectionState) {
                                    LiveKitConnectionState.CONNECTED -> androidx.compose.ui.graphics.Color(0xFFE7F7E7)
                                    LiveKitConnectionState.CONNECTING -> androidx.compose.ui.graphics.Color(0xFFFFF4E6)
                                    LiveKitConnectionState.FAILED -> androidx.compose.ui.graphics.Color(0xFFFFE6E6)
                                    else -> androidx.compose.ui.graphics.Color(0xFFF0F0F0)
                                },
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (connectionState) {
                                LiveKitConnectionState.CONNECTED -> {
                                    val roleText = if (isHost) "HOST" else "VIEWER"
                                    if (participantCount > 1) {
                                        "✓ Connected ($participantCount devices) • $roleText"
                                    } else {
                                        "✓ Ready for pairing • $roleText"
                                    }
                                }
                                LiveKitConnectionState.CONNECTING -> "⏳ Connecting..."
                                LiveKitConnectionState.FAILED -> "✗ Connection failed"
                                LiveKitConnectionState.DISCONNECTED -> "📱 Ready to pair"
                                else -> ""
                            },
                            fontSize = 14.sp,
                            color = when (connectionState) {
                                LiveKitConnectionState.CONNECTED -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                                LiveKitConnectionState.CONNECTING -> androidx.compose.ui.graphics.Color(0xFFFF9800)
                                LiveKitConnectionState.FAILED -> androidx.compose.ui.graphics.Color(0xFFF44336)
                                else -> androidx.compose.ui.graphics.Color.Gray
                            }
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Tab Selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .background(androidx.compose.ui.graphics.Color(0xFFF3F0F8))
                        .padding(4.dp)
                ) {
                    TabItem(
                        text = "Show QR Code",
                        selected = selectedTab == "Show QR Code",
                        modifier = Modifier.weight(1f),
                        icon = painterResource(id = R.drawable.ic_qr_scan),
                        onClick = { selectedTab = "Show QR Code" }
                    )
                    TabItem(
                        text = "Scan QR Code",
                        selected = selectedTab == "Scan QR Code",
                        modifier = Modifier.weight(1f),
                        icon = painterResource(id = R.drawable.ic_camera),
                        onClick = { selectedTab = "Scan QR Code" }
                    )
                }
                Spacer(Modifier.height(20.dp))

                // Content based on selected tab
                if (selectedTab == "Show QR Code") {
                    // QR Code Display
                    qrCodeBitmap?.let { bitmap ->
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(androidx.compose.ui.graphics.Color.White)
                                .border(1.dp, androidx.compose.ui.graphics.Color.LightGray, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.size(180.dp)
                            )
                        }
                    } ?: Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(androidx.compose.ui.graphics.Color(0xFFF5F5F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = androidx.compose.ui.graphics.Color(0xFF9B8AFB)
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = if (isHost && connectionState == LiveKitConnectionState.CONNECTED && participantCount > 1) {
                            "Device already connected! Share this QR code with more devices to monitor remotely."
                        } else {
                            "Share this QR code with another device to connect and monitor baby remotely"
                        },
                        textAlign = TextAlign.Center,
                        color = androidx.compose.ui.graphics.Color(0xFF4B5563),
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(20.dp))

                    // Instructions
                    InstructionItem(1, "Open BubTrack app on the other device")
                    InstructionItem(2, "Go to Baby Monitor AI")
                    InstructionItem(3, "Tap 'Pair Device' and select 'Scan QR Code'")
                    InstructionItem(4, "Point camera at this QR code")
                    InstructionItem(5, "View live feed and receive status updates")
                    Spacer(Modifier.height(24.dp))

                    // Action Buttons
                    Button(
                        onClick = {
                            try {
                                val roomInfo = sleepViewModel.generateRoomInfo()
                                val shareIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, "Join my baby monitor session: $roomInfo")
                                    type = "text/plain"
                                }
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Room"))
                            } catch (e: Exception) {
                                Log.e("DevicePairingPopup", "Failed to share room info: ${e.message}")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF9B8AFB)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_share),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Share Room Info", color = androidx.compose.ui.graphics.Color.White, fontSize = 14.sp)
                    }
                } else {
                    // QR Scanner
                    if (hasCameraPermission) {
                        QRScanner(
                            modifier = Modifier.size(300.dp),
                            onScanSuccess = { scannedData ->
                                scanResult = scannedData
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(300.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(androidx.compose.ui.graphics.Color(0xFFF5F5F5))
                                .border(1.dp, androidx.compose.ui.graphics.Color.LightGray, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_camera),
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = androidx.compose.ui.graphics.Color.Gray
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Camera Permission Required",
                                    color = androidx.compose.ui.graphics.Color.Gray,
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF9B8AFB))
                                ) {
                                    Text("Grant Permission", color = androidx.compose.ui.graphics.Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Point your camera at the QR code displayed on the host device to connect as a viewer",
                        textAlign = TextAlign.Center,
                        color = androidx.compose.ui.graphics.Color(0xFF4B5563),
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalGetImage::class)
@Composable
private fun QRScanner(
    modifier: Modifier = Modifier,
    onScanSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }
    var isScanned by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(executor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null && !isScanned) {
                            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            barcodeScanner.process(inputImage)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        if (barcode.format == Barcode.FORMAT_QR_CODE) {
                                            val rawValue = barcode.rawValue
                                            if (rawValue != null) {
                                                onScanSuccess(rawValue)
                                                isScanned = true
                                                Log.d("QRScanner", "Scanned QR code: $rawValue")
                                                break
                                            }
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("QRScanner", "Barcode scanning failed: ${e.message}")
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("QRScanner", "Camera binding failed: ${exc.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, androidx.compose.ui.graphics.Color(0xFF9B8AFB), RoundedCornerShape(12.dp))
    )
}

private fun generateQRCode(text: String, size: Int): Bitmap {
    val bitMatrix: com.google.zxing.common.BitMatrix = com.google.zxing.MultiFormatWriter().encode(
        text,
        com.google.zxing.BarcodeFormat.QR_CODE,
        size,
        size
    )
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(
                x,
                y,
                if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            )
        }
    }
    return bitmap
}

@Composable
fun TabItem(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) androidx.compose.ui.graphics.Color(0xFF9B8AFB) else androidx.compose.ui.graphics.Color.Transparent
    val textColor = if (selected) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun InstructionItem(number: Int, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(androidx.compose.ui.graphics.Color(0xFF93C5FD)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                fontSize = 12.sp,
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = androidx.compose.ui.graphics.Color(0xFF374151),
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(12.dp))
}