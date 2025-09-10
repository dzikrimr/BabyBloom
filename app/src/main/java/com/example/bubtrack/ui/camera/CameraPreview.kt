package com.example.bubtrack.ui.camera

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.bubtrack.viewmodel.SleepDetectionViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    viewModel: SleepDetectionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    var previewView: PreviewView? by remember { mutableStateOf(null) }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).also {
                previewView = it
                setupCamera(ctx, it, lifecycleOwner, viewModel, cameraExecutor)
            }
        },
        modifier = modifier,
        update = { view ->
            // Ensure PreviewView is reused across recompositions
            previewView = view
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            Log.d("CameraPreview", "Camera executor shut down")
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun setupCamera(
    context: android.content.Context,
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    viewModel: SleepDetectionViewModel,
    cameraExecutor: ExecutorService
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder()
            .setTargetResolution(android.util.Size(640, 480)) // Lowered resolution
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        val imageAnalyzer = ImageAnalysis.Builder()
            .setTargetResolution(android.util.Size(640, 480)) // Lowered resolution
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    // Throttle to ~1 FPS
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastProcessedTime >= 1000) { // 1000ms = ~1 FPS
                        processImage(imageProxy, viewModel)
                        lastProcessedTime = currentTime
                    } else {
                        imageProxy.close()
                        Log.d("CameraPreview", "Frame skipped due to throttling")
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
            Log.d("CameraPreview", "Camera bound successfully with back camera")
        } catch (e: Exception) {
            Log.e("CameraPreview", "Camera binding failed: ${e.message}", e)
        }
    }, ContextCompat.getMainExecutor(context))
}

private var lastProcessedTime = 0L

@ExperimentalGetImage
private fun processImage(imageProxy: ImageProxy, viewModel: SleepDetectionViewModel) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        Log.d("CameraPreview", "Processing frame with rotation: $rotationDegrees")
        viewModel.processFrame(imageProxy) // Pass imageProxy directly
    } else {
        Log.e("CameraPreview", "No media image available")
        imageProxy.close()
    }
}