package com.example.bubtrack.presentation.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.bubtrack.R
import com.example.bubtrack.domain.activities.ActivitiesRepo
import com.example.bubtrack.domain.activities.Activity
import com.example.bubtrack.domain.diary.DiaryRepo
import com.example.bubtrack.presentation.notification.comps.NotificationItem
import com.example.bubtrack.utill.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activitiesRepo: ActivitiesRepo,
    private val diaryRepo: DiaryRepo,
    private val fcmRepo: FcmRepo
) {

    companion object {
        private const val CHANNEL_ID = "BUBTRACK_NOTIFICATIONS"
        private const val DIARY_REMINDER_WORK = "diary_reminder_work"
        private const val ACTIVITY_REMINDER_WORK = "activity_reminder_work"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "BubTrack Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for activities and diary reminders"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleDiaryReminders() {
        val workManager = WorkManager.getInstance(context)

        // Cancel existing work
        workManager.cancelUniqueWork(DIARY_REMINDER_WORK)

        // Schedule diary reminders 3 times a week (Monday, Wednesday, Friday at 8 PM)
        val diaryReminderWork = PeriodicWorkRequestBuilder<DiaryReminderWorker>(
            1, TimeUnit.DAYS
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        ).build()

        workManager.enqueueUniquePeriodicWork(
            DIARY_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.REPLACE,
            diaryReminderWork
        )
    }

    fun observeUpcomingActivities(): Flow<List<Activity>> = flow {
        activitiesRepo.getAllActivities().collect { resource ->
            if (resource is Resource.Success) {
                val activities = resource.data ?: emptyList()
                val upcomingActivities = activities.filter { activity ->
                    val activityDateTime = LocalDateTime.ofEpochSecond(
                        activity.date / 1000,
                        0,
                        ZoneId.systemDefault().rules.getOffset(
                            LocalDateTime.ofEpochSecond(activity.date / 1000, 0)
                        )
                    )
                    val now = LocalDateTime.now()
                    val timeDiff = java.time.Duration.between(now, activityDateTime)

                    // Show reminder for activities within next 24 hours
                    timeDiff.toHours() in 0..24
                }

                upcomingActivities.forEach { activity ->
                    scheduleActivityReminder(activity)
                }

                emit(upcomingActivities)
            }
        }
    }

    private fun scheduleActivityReminder(activity: Activity) {
        val workManager = WorkManager.getInstance(context)

        val activityDateTime = LocalDateTime.ofEpochSecond(
            activity.date / 1000,
            0,
            ZoneId.systemDefault().rules.getOffset(
                LocalDateTime.ofEpochSecond(activity.date / 1000, 0)
            )
        )

        val reminderTime = activityDateTime.minusHours(1) // 1 hour before
        val now = LocalDateTime.now()

        if (reminderTime.isAfter(now)) {
            val delay = java.time.Duration.between(now, reminderTime).toMinutes()

            val data = Data.Builder()
                .putString("activity_title", activity.title)
                .putString("activity_description", activity.description)
                .putString("activity_id", activity.id.toString())
                .putString("activity_type", activity.type)
                .build()

            val reminderWork = OneTimeWorkRequestBuilder<ActivityReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MINUTES)
                .setInputData(data)
                .build()

            workManager.enqueueUniqueWork(
                "activity_reminder_${activity.id}",
                ExistingWorkPolicy.REPLACE,
                reminderWork
            )
        }
    }

    fun showNotification(title: String, message: String, type: String, activityId: String? = null) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, Class.forName("com.example.bubtrack.MainActivity")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)

        // Save to Firestore
        val notificationItem = NotificationItem(
            id = notificationId.toString(),
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            type = type,
            activityId = activityId
        )

        // Launch coroutine to save notification
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            fcmRepo.saveNotification(notificationItem)
        }
    }
}
