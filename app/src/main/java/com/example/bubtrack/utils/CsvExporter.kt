package com.example.bubtrack.utils

import com.example.bubtrack.data.database.SleepSessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class CsvExporter {

    suspend fun exportSessionToCsv(
        session: SleepSessionEntity,
        outputDir: File
    ): String = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = dateFormat.format(Date(session.startTime))
        val fileName = "sleep_session_$timestamp.csv"
        val file = File(outputDir, fileName)

        FileWriter(file).use { writer ->
            // Header
            writer.write("session_id,start_time,end_time,duration_sec,average_ear,average_mar,average_movement,rollover_count,pacifier_usage,total_frames\n")

            // Data
            val duration = (session.endTime ?: System.currentTimeMillis()) - session.startTime
            writer.write("${session.id},${session.startTime},${session.endTime ?: "ongoing"},${duration/1000},${session.averageEAR},${session.averageMAR},${session.averageMovement},${session.rolloverCount},${session.pacifierUsage},${session.totalFramesProcessed}\n")
        }

        file.absolutePath
    }
}