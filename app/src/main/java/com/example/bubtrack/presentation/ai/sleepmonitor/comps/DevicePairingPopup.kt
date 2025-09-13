package com.example.bubtrack.presentation.ai.sleepmonitor.comps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
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
import com.example.bubtrack.data.webrtc.WebRTCService
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.delay
import org.webrtc.SurfaceViewRenderer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun DevicePairingPopup(
    onDismiss: () -> Unit,
    webRTCService: WebRTCService,
    localRenderer: SurfaceViewRenderer,
    remoteRenderer: SurfaceViewRenderer,
    onPairingSuccess: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedTab by remember { mutableStateOf("Show QR Code") }
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<String?>(null) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
    var cameraExecutor: ExecutorService? by remember { mutableStateOf(null) }
    var showConnectionStatus by remember { mutableStateOf(false) }

    val connectionState by webRTCService.connectionState.collectAsState()
    val pairedDeviceInfo by webRTCService.pairedDeviceInfo.collectAsState()

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted && selectedTab == "Scan QR Code") {
            isScanning = true
        }
    }

    // Check camera permission
    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    // Generate QR code for host
    LaunchedEffect(selectedTab) {
        if (selectedTab == "Show QR Code") {
            val pairingData = webRTCService.startAsHost(localRenderer)
            qrCodeBitmap = generateQRCode("bubtrack://pair?session=$pairingData", 400, 400)
        } else if (selectedTab == "Scan QR Code" && hasCameraPermission) {
            isScanning = true
        }
    }

    // Handle scanning when tab switches to scan
    LaunchedEffect(selectedTab, hasCameraPermission) {
        if (selectedTab == "Scan QR Code") {
            if (hasCameraPermission) {
                isScanning = true
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        } else {
            isScanning = false
            cameraProvider?.unbindAll()
        }
    }

    // Handle scan result
    LaunchedEffect(scanResult) {
        scanResult?.let { result ->
            Log.d("DevicePairingPopup", "QR Code scanned: $result")
            showConnectionStatus = true
            try {
                webRTCService.joinAsClient(result, localRenderer, remoteRenderer)
                onPairingSuccess(result)
            } catch (e: Exception) {
                Log.e("DevicePairingPopup", "Failed to join as client: ${e.message}")
            }
        }
    }

    // Monitor connection state
    LaunchedEffect(connectionState) {
        when (connectionState) {
            com.example.bubtrack.data.webrtc.ConnectionState.CONNECTED -> {
                showConnectionStatus = true
                delay(2000) // Show success message for 2 seconds
                onDismiss()
            }
            com.example.bubtrack.data.webrtc.ConnectionState.FAILED -> {
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
                        color = Color(0xFF1F2937),
                        modifier = Modifier.align(Alignment.Center)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .background(Color(0xFFF3F4F6), CircleShape)
                            .size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color(0xFFF3F0F8))
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
                if (selectedTab == "Show QR Code") {
                    qrCodeBitmap?.let { bitmap ->
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.size(160.dp)
                            )
                        }
                    } ?: Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Generating QR Code...",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Scan to connect and monitor baby remotely",
                        textAlign = TextAlign.Center,
                        color = Color(0xFF4B5563),
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    InstructionItem(1, "Open BabyBloom app on the other device")
                    InstructionItem(2, "Tap \"Pair Device\" and scan this QR code")
                    InstructionItem(3, "View live feed and receive notifications")
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { /* Share logic - Implement sharing of QR or link */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B8AFB)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_share),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Share QR Code", color = Color.White, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(5.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { /* Save logic - Implement saving QR to gallery */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF3F4F6),
                                contentColor = Color(0xFF9F9F9F)
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_save),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF374151)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Save", color = Color(0xFF374151), fontSize = 12.sp)
                        }
                        Button(
                            onClick = { /* Copy logic - Implement copying link to clipboard */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF3F4F6),
                                contentColor = Color(0xFF9F9F9F)
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_copy),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF374151)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Copy Link", color = Color(0xFF374151), fontSize = 11.sp)
                        }
                    }
                } else {
                    if (hasCameraPermission) {
                        QRScanner(
                            onScanSuccess = { scannedData ->
                                scanResult = scannedData
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(300.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Camera Permission Required",
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Point your camera at the QR code on the other device to connect",
                        textAlign = TextAlign.Center,
                        color = Color(0xFF4B5563),
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (showConnectionStatus) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = when (connectionState) {
                            com.example.bubtrack.data.webrtc.ConnectionState.CONNECTED -> "Connected to ${pairedDeviceInfo?.deviceName ?: "device"}"
                            com.example.bubtrack.data.webrtc.ConnectionState.FAILED -> "Connection failed"
                            else -> "Connecting..."
                        },
                        color = if (connectionState == com.example.bubtrack.data.webrtc.ConnectionState.CONNECTED) Color(0xFF10B981)
                        else if (connectionState == com.example.bubtrack.data.webrtc.ConnectionState.FAILED) Color.Red
                        else Color(0xFF4B5563),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor?.shutdown()
            cameraProvider?.unbindAll()
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalGetImage::class)
@Composable
private fun QRScanner(
    onScanSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    var preview by remember { mutableStateOf<Preview?>(null) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }
    var isScanned by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            preview = Preview.Builder().build().also {
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
                                            if (rawValue != null && rawValue.startsWith("bubtrack://pair?session=")) {
                                                val sessionData = rawValue.substringAfter("session=")
                                                onScanSuccess(sessionData)
                                                isScanned = true
                                                Log.d("QRScanner", "Scanned session data: $sessionData")
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
        modifier = Modifier
            .size(300.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
    )
}

private fun generateQRCode(text: String, width: Int, height: Int): Bitmap {
    val bitMatrix: BitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(
                x,
                y,
                if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE
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
    val backgroundColor = if (selected) Color(0xFF9B8AFB) else Color.Transparent
    val textColor = if (selected) Color.White else Color.Black.copy(alpha = 0.6f)
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
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 4.dp)
                )
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
                .background(Color(0xFF93C5FD)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF374151),
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(12.dp))
}

@SuppressLint("ViewModelConstructorInComposable")
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DevicePairingPopupPreview() {
    MaterialTheme {
        DevicePairingPopup(
            onDismiss = {},
            webRTCService = WebRTCService(LocalContext.current),
            localRenderer = SurfaceViewRenderer(LocalContext.current),
            remoteRenderer = SurfaceViewRenderer(LocalContext.current)
        )
    }
}