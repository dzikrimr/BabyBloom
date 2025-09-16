package com.example.bubtrack.presentation.ai.cobamonitor

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import kotlinx.coroutines.delay
import org.webrtc.SurfaceViewRenderer

@Composable
fun ParentScreen(
    viewModel: MonitorRoomViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var roomId by remember { mutableStateOf<String?>(null) }
    var isMonitoring by remember { mutableStateOf(false) }
    var monitoringTime by remember { mutableStateOf("00:00:00") }
    var isPreviewVisible by remember { mutableStateOf(true) }
    var showRoomIdCopied by remember { mutableStateOf(false) }
    var isSurfaceReady by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header with Back Button
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
                        modifier = Modifier.fillMaxSize(),
                        tint = Color(0xFF2D3748)
                    )
                }
                Text(
                    "Parent Device (Monitor)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2D3748)
                )
                Spacer(modifier = Modifier.width(25.dp))
            }

            Text(
                text = if (isMonitoring) "Monitoring Active" else "Ready to Monitor",
                fontSize = 14.sp,
                color = Color(0xFF718096),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }

        // ================= MONITORING MODE =================
        if (isMonitoring) {
            // Header with RoomId + Timer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF8B5CF6))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = monitoringTime,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Video Preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp)),
                        factory = { ctx ->
                            Log.d("ParentScreen", "Creating remote SurfaceViewRenderer")
                            val renderer = SurfaceViewRenderer(ctx).apply {
                                setMirror(false)
                                setEnableHardwareScaler(true)
                            }
                            viewModel.initRemoteSurface(renderer)
                            isSurfaceReady = true
                            renderer
                        }
                    )

                    // Status overlays
                    when {
                        !isPreviewVisible -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.8f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_eye_off),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Monitor Feed Hidden",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        state is MonitorState.WaitingForBaby -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.9f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 3.dp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Waiting for Baby Device",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Share Room ID with baby device",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        state is MonitorState.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFEF4444).copy(alpha = 0.9f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Error: ${(state as MonitorState.Error).msg}",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        state is MonitorState.Connected -> {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(16.dp)
                                    .background(
                                        Color(0xFF10B981).copy(alpha = 0.9f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Live", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    // Toggle visibility
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .clickable { isPreviewVisible = !isPreviewVisible },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isPreviewVisible) R.drawable.ic_eye else R.drawable.ic_eye_off
                            ),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            if (roomId != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Room ID",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = roomId!!,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8B5CF6)
                            )
                        }

                        Icon(
                            painter = painterResource(id = R.drawable.ic_copy),
                            contentDescription = "Copy Room ID",
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(roomId!!))
                                    showRoomIdCopied = true
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        roomId?.let {
                            clipboardManager.setText(AnnotatedString(it))
                            showRoomIdCopied = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF60A5FA)),
                    shape = RoundedCornerShape(12.dp),
                    enabled = roomId != null
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_copy),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Room ID", color = Color.White)
                }

                Button(
                    onClick = {
                        isMonitoring = false
                        roomId?.let { viewModel.stopMonitoring(it) }
                        roomId = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_stop),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stop Monitoring", color = Color.White)
                }
            }
        }
        // ================= SETUP MODE =================
        else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_eye),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFF8B5CF6)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Mulai Baby Monitor",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Buat ruang monitoring dan bagikan Room ID ke perangkat bayi Anda",
                        fontSize = 16.sp,
                        color = Color(0xFF718096),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (roomId == null) {
                                viewModel.createRoom { generatedId ->
                                    roomId = generatedId
                                    isMonitoring = true // langsung masuk monitoring
                                }
                            } else {
                                isMonitoring = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        shape = RoundedCornerShape(16.dp),
                        enabled = isSurfaceReady
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_add),
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Buat Monitor Room",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Hidden SurfaceView init
            AndroidView(
                modifier = Modifier.size(0.dp),
                factory = { ctx ->
                    Log.d("ParentScreen", "Creating hidden SurfaceViewRenderer")
                    val renderer = SurfaceViewRenderer(ctx).apply {
                        setMirror(false)
                        setEnableHardwareScaler(true)
                    }
                    viewModel.initRemoteSurface(renderer)
                    isSurfaceReady = true
                    renderer
                }
            )
        }
    }
}
