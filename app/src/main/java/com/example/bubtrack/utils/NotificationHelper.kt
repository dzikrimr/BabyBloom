package com.example.bubtrack.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "baby_monitor_alerts"
        const val ROLLOVER_NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Baby Monitor Alerts"
            val descriptionText = "Alerts for baby safety monitoring"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendRolloverAlert() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val builder = NotificationCompat.Builder(this@NotificationHelper.context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("⚠️ PERINGATAN!")
                .setContentText("Bayi mungkin dalam posisi rollover. Periksa segera!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 500, 200, 500))

            NotificationManagerCompat.from(this@NotificationHelper.context)
                .notify(ROLLOVER_NOTIFICATION_ID, builder.build())
        } else {
            // Optionally log or handle the lack of permission
        }
    }

    fun sendMovementAlert() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val builder = NotificationCompat.Builder(this@NotificationHelper.context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("👶 Info")
                .setContentText("Bayi menunjukkan gerakan aktif")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            NotificationManagerCompat.from(this@NotificationHelper.context)
                .notify(1002, builder.build())
        } else {
            // Optionally log or handle the lack of permission
        }
    }
}