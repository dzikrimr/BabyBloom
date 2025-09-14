package com.example.bubtrack.data.ai

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

interface AudioRepository {
    suspend fun recordAudio(): ShortArray?
}

class AudioRepoImpl(
    private val context: Context,
    private val isRecordingState: () -> Boolean
) : AudioRepository {

    override suspend fun recordAudio(): ShortArray? = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED) {
            return@withContext null
        }

        val sampleRate = 22050
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT

        val minBuffer = AudioRecord.getMinBufferSize(
            sampleRate,
            channelConfig,
            audioFormat
        ).coerceAtLeast(4096)

        val audioRecord = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                minBuffer * 2 // Use larger buffer
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
            return@withContext null
        }

        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            audioRecord.release()
            return@withContext null
        }

        val maxSeconds = 5
        val maxSamples = sampleRate * maxSeconds
        val bufferSize = minBuffer / 4 // Smaller read chunks for responsiveness
        val buf = ShortArray(bufferSize)
        val recorded = mutableListOf<Short>()

        try {
            audioRecord.startRecording()

            // Wait a bit for recording to stabilize
            delay(100)

            while (isRecordingState() && recorded.size < maxSamples) {
                val read = audioRecord.read(buf, 0, buf.size)

                if (read > 0) {
                    // Add samples to list
                    for (i in 0 until read) {
                        if (recorded.size < maxSamples) {
                            recorded.add(buf[i])
                        } else {
                            break
                        }
                    }
                } else if (read == AudioRecord.ERROR_INVALID_OPERATION) {
                    println("AudioRecord: Invalid operation")
                    break
                } else if (read == AudioRecord.ERROR_BAD_VALUE) {
                    println("AudioRecord: Bad value")
                    break
                }

                // Small delay to prevent tight loop
                if (isRecordingState()) {
                    delay(10)
                }
            }

            audioRecord.stop()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            audioRecord.release()
        }

        // Check minimum recording length (0.5 seconds)
        val minSamples = sampleRate / 2
        if (recorded.size < minSamples) {
            println("Recording too short: ${recorded.size} samples")
            return@withContext null
        }

        println("Recorded ${recorded.size} samples")
        return@withContext recorded.toShortArray()
    }
}