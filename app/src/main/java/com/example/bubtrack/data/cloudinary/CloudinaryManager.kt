package com.example.bubtrack.data.cloudinary

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class CloudinaryManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var isCloudinaryInitialized = false

    init {
        initializeCloudinary()
    }

    private fun initializeCloudinary() {
        if (!isCloudinaryInitialized) {
            try {
                MediaManager.init(context, mapOf(
                    "cloud_name" to "dvwbrl4el",
                    "api_key" to "132211812476492",
                    "api_secret" to "LJm1DiUIlp4E_6jSI90EaIVFAFU"
                ))
                isCloudinaryInitialized = true
            } catch (e: Exception) {
                // Handle initialization error silently in production
            }
        }
    }

    suspend fun uploadImage(imageUri: String): Result<String?> = suspendCoroutine { continuation ->
        if (!isCloudinaryInitialized) {
            continuation.resume(Result.failure(Exception("Cloudinary belum diinisialisasi")))
            return@suspendCoroutine
        }

        try {
            MediaManager.get().upload(Uri.parse(imageUri))
                .option("folder", "bubtrack_profiles")
                .option("resource_type", "image")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        // Optional: Show upload started message
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        // Optional: Show progress
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val imageUrl = resultData["secure_url"] as? String
                        continuation.resume(Result.success(imageUrl))
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        continuation.resume(Result.failure(Exception(error.description)))
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        continuation.resume(Result.failure(Exception("Upload rescheduled: ${error.description}")))
                    }
                }).dispatch(context)
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }
}