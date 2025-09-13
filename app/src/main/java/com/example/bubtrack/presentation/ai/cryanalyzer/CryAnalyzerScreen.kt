package com.example.bubtrack.presentation.ai.cryanalyzer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.min

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CryAnalyzerScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isRecording by remember { mutableStateOf(false) }
    var classificationResult by remember { mutableStateOf("Tekan tombol untuk merekam") }
    val labels = listOf(
        "belly_pain", "burping", "cold_hot", "discomfort", "hungry",
        "laugh", "silence", "tired"
    )
    var confidenceScores by remember {
        mutableStateOf(
            labels.map { label -> label to 0f }
        )
    }
    val permissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted
        } else {
            classificationResult = "Izin rekam audio diperlukan."
        }
    }

    val interpreter = remember { loadModel(context) }

    fun toggleRecording() {
        if (!permissionState.status.isGranted) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        isRecording = !isRecording
        if (isRecording) {
            classificationResult = "Merekam..."
            coroutineScope.launch {
                val audioData = recordAudio(context, isRecordingState = { isRecording })
                if (audioData != null) {
                    try {
                        val (label, scores) = classifyAudio(interpreter, audioData)
                        classificationResult = "Hasil: $label"
                        confidenceScores = scores
                    } catch (e: Exception) {
                        classificationResult = "Error klasifikasi: ${e.message}"
                        e.printStackTrace()
                    }
                } else {
                    classificationResult = "Gagal merekam audio."
                }
                isRecording = false
            }
        } else {
            classificationResult = "Rekaman dihentikan."
        }
    }

    var seconds by remember { mutableStateOf(0) }

    // Timer jalan kalau lagi recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (true) {
                delay(1000L)
                seconds++
            }
        } else {
            // reset kalau mau balik ke 0 saat stop
            // kalau mau tetap lanjut hitungan total, hapus reset ini
            seconds = 0
        }
    }

    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, remainingSeconds)

    Column(
        modifier = Modifier
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
                        color = if (isRecording) Color(0xFFF87171) else Color.Gray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(percent = 50)
                    )
                    .scale(if (isRecording) scale else 1f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_speaker),
                    "record button",
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
                Spacer(modifier.height(8.dp))
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
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(12.dp)
        ) {
            Text(
                "Baby's need analysis",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier.height(8.dp))
            BabyNeedPager(
                results = confidenceScores
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {toggleRecording()},
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
                        id = if (isRecording) R.drawable.ic_muted else R.drawable.ic_mic,
                    ),
                    contentDescription = "mic button"
                )
                Spacer(modifier.width(5.dp))
                Text(
                    text = if (isRecording) "Stop Recording" else "Start Recording",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

private fun loadModel(context: Context): Interpreter {
    val assetManager = context.assets
    val modelFile = assetManager.openFd("baby_cry_model.tflite")
    val fileChannel = FileInputStream(modelFile.fileDescriptor).channel
    val mappedBuffer: MappedByteBuffer = fileChannel.map(
        FileChannel.MapMode.READ_ONLY,
        modelFile.startOffset,
        modelFile.declaredLength
    )
    return Interpreter(mappedBuffer)
}

private suspend fun recordAudio(context: Context, isRecordingState: () -> Boolean): FloatArray? =
    withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return@withContext null
        }

        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val minSamples = sampleRate // Require at least 1 second of audio
        val maxSamples = sampleRate * 30 // Max 30 seconds to prevent memory issues

        try {
            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord.release()
                return@withContext null
            }

            val audioData = mutableListOf<Short>()
            val tempBuffer = ShortArray(bufferSize)

            audioRecord.startRecording()
            while (isRecordingState() && audioData.size < maxSamples) {
                val read = audioRecord.read(tempBuffer, 0, tempBuffer.size)
                if (read > 0) {
                    audioData.addAll(tempBuffer.take(read))
                } else {
                    break // Error reading audio
                }
            }
            audioRecord.stop()
            audioRecord.release()

            if (audioData.size < minSamples) {
                println("Audio too short: ${audioData.size} samples, required $minSamples")
                return@withContext null
            }

            val result = audioData.map { it.toFloat() / Short.MAX_VALUE }.toFloatArray()
            println("Recorded samples: ${result.size}")
            result
        } catch (e: SecurityException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

private fun classifyAudio(
    interpreter: Interpreter,
    audioData: FloatArray
): Pair<String, List<Pair<String, Float>>> {
    val inputSize = 40
    val preprocessedData = preprocessAudio(audioData, inputSize)

    // FIX UTAMA: Input tensor shape (1, 40, 1) - 3D tensor
    val inputBuffer = ByteBuffer.allocateDirect(1 * inputSize * 1 * 4)
        .order(ByteOrder.nativeOrder())

    // Fill dengan shape yang benar
    for (i in 0 until inputSize) {
        inputBuffer.putFloat(preprocessedData[i])
    }
    inputBuffer.rewind()

    // Output shape validation
    val outputTensor = interpreter.getOutputTensor(0)
    val outputShape = outputTensor.shape()
    val numClasses = outputShape[1] // Should be 9

    val output = Array(1) { FloatArray(numClasses) }
    interpreter.run(inputBuffer, output)

    // Labels yang benar (9 kelas sesuai Python)
    val labels = listOf(
        "belly_pain", "burping", "cold_hot", "discomfort", "hungry",
        "laugh", "silence", "tired"
    )

    val scores = output[0].mapIndexed { index, score ->
        labels[index] to score
    }.sortedByDescending { it.second }

    return scores.first().first to scores
}

// Preprocessing tetap sama (cukup untuk testing awal)
private fun preprocessAudio(audioData: FloatArray, targetSize: Int = 40): FloatArray {
    if (audioData.isEmpty()) return FloatArray(targetSize)

    // Simple energy-based features (untuk testing dulu)
    val segmentSize = audioData.size / targetSize
    val result = FloatArray(targetSize)

    for (i in 0 until targetSize) {
        val start = i * segmentSize
        val end = minOf((i + 1) * segmentSize, audioData.size)
        if (start < end) {
            val segment = audioData.sliceArray(start until end)
            result[i] = segment.map { it * it }.average().toFloat()
        }
    }

    // Normalize
    val maxEnergy = result.maxOrNull() ?: 1f
    if (maxEnergy > 0) {
        for (i in result.indices) {
            result[i] /= maxEnergy
        }
    }

    return result
}