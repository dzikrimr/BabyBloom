package com.example.bubtrack.presentation.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bubtrack.data.notification.NotificationScheduler
import com.example.bubtrack.domain.diary.DiaryRepo
import com.example.bubtrack.utill.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

@HiltWorker
class DiaryReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val diaryRepo: DiaryRepo,
    private val notificationScheduler: NotificationScheduler
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val today = LocalDate.now()
            val dayOfWeek = today.dayOfWeek

            // Check if today is Monday, Wednesday, or Friday
            if (dayOfWeek in listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)) {
                // Check if user has written diary in last 3 days
                val hasRecentDiary = checkRecentDiaryEntries()

                if (!hasRecentDiary) {
                    notificationScheduler.showNotification(
                        title = "📖 Diary Reminder",
                        message = "Don't forget to record your baby's special moments today!",
                        type = "DIARY_REMINDER"
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun checkRecentDiaryEntries(): Boolean {
        return try {
            val threeDaysAgo = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000L)
            val result = diaryRepo.getAllDiaries()

            if (result is Resource.Success) {
                val diaries = result.data ?: emptyList()
                diaries.any { it.date >= threeDaysAgo }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}