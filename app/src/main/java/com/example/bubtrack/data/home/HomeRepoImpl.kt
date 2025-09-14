package com.example.bubtrack.data.home

import com.example.bubtrack.domain.activities.Activity
import com.example.bubtrack.domain.growth.GrowthStats
import com.example.bubtrack.domain.home.HomeRepo
import com.example.bubtrack.domain.profile.BabyProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class HomeRepoImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : HomeRepo {

    override suspend fun getBabyProfile(userId: String): Result<BabyProfile> {
        return try {
            val document = firestore.collection("users").document(userId)
                .collection("babyProfiles").document("primary")
                .get()
                .await()

            if (document.exists()) {
                val data = document.data!!

                val profile = BabyProfile(
                    babyName = data["babyName"] as? String ?: "Sarah",
                    dateMillis = document.getLong("birthDate") ?: 0L,
                    selectedGender = data["selectedGender"] as? String ?: "",
                    weight = data["weight"] as? String ?: "",
                    height = data["height"] as? String ?: "",
                    headCircumference = data["headCircumference"] as? String ?: "",
                    armCircumference = data["armCircumference"] as? String ?: ""
                )
                Result.success(profile)
            } else {
                Result.failure(Exception("Profil bayi tidak ditemukan!"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLatestGrowthStats(userId: String): Result<GrowthStats> {
        return try {
            val snapshot = firestore.collection("users").document(userId)
                .collection("growthRecords")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val latestGrowth = snapshot.documents.firstOrNull()?.let { doc ->
                GrowthStats(
                    weight = doc.getDouble("weight") ?: 0.0,
                    height = doc.getDouble("height") ?: 0.0,
                    headCircum = doc.getDouble("headCircumference") ?: 0.0,
                    armCircum = doc.getDouble("armLength") ?: 0.0
                )
            } ?: GrowthStats()

            Result.success(latestGrowth)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUpcomingActivities(userId: String): Result<List<Activity>> {
        return try {
            val snapshot = firestore.collection("users").document(userId)
                .collection("activities")
                .get()
                .await()

            val today = LocalDate.now()
            val sevenDaysFromNow = today.plusDays(7)

            val activities = snapshot.documents.mapNotNull { doc ->
                try {
                    Activity(
                        id = doc.getLong("id")?.toInt() ?: 0,
                        userId = doc.getString("userId") ?: "",
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        date = doc.getLong("date") ?: 0L,
                        hour = doc.getLong("hour")?.toInt() ?: 0,
                        minute = doc.getLong("minute")?.toInt() ?: 0,
                        type = doc.getString("type") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }.filter { activity ->
                val activityDate = Instant.ofEpochMilli(activity.date)
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                !activityDate.isBefore(today) && !activityDate.isAfter(sevenDaysFromNow)
            }.sortedBy { it.date }

            Result.success(activities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeGrowthStats(userId: String): Flow<Result<GrowthStats>> =
        callbackFlow {
            val listener = firestore.collection("users").document(userId)
                .collection("growthRecords")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Result.failure(error))
                        return@addSnapshotListener
                    }

                    val latestGrowth = snapshot?.documents?.firstOrNull()?.let { doc ->
                        GrowthStats(
                            weight = doc.getDouble("weight") ?: 0.0,
                            height = doc.getDouble("height") ?: 0.0,
                            headCircum = doc.getDouble("headCircumference") ?: 0.0,
                            armCircum = doc.getDouble("armLength") ?: 0.0
                        )
                    } ?: GrowthStats()

                    trySend(Result.success(latestGrowth))
                }

            awaitClose { listener.remove() }
        }

    override fun observeUpcomingActivities(userId: String): Flow<Result<List<Activity>>> =
        callbackFlow {
            val listener = firestore.collection("users").document(userId)
                .collection("activities")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Result.failure(error))
                        return@addSnapshotListener
                    }

                    val today = LocalDate.now()
                    val sevenDaysFromNow = today.plusDays(7)

                    val activities = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            Activity(
                                id = doc.getLong("id")?.toInt() ?: 0,
                                userId = doc.getString("userId") ?: "",
                                title = doc.getString("title") ?: "",
                                description = doc.getString("description") ?: "",
                                date = doc.getLong("date") ?: 0L,
                                hour = doc.getLong("hour")?.toInt() ?: 0,
                                minute = doc.getLong("minute")?.toInt() ?: 0,
                                type = doc.getString("type") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }?.filter { activity ->
                        val activityDate = Instant.ofEpochMilli(activity.date)
                            .atZone(ZoneId.systemDefault()).toLocalDate()
                        !activityDate.isBefore(today) && !activityDate.isAfter(sevenDaysFromNow)
                    }?.sortedBy { it.date } ?: emptyList()

                    trySend(Result.success(activities))
                }

            awaitClose { listener.remove() }
        }

    // Utility function untuk calculate age
    override fun calculateAge(birthDateMillis: Long): String {
        if (birthDateMillis == 0L) return ""

        val birthDate = Instant.ofEpochMilli(birthDateMillis)
            .atZone(ZoneId.systemDefault()).toLocalDate()
        val today = LocalDate.now()
        val months = ChronoUnit.MONTHS.between(birthDate, today).toInt()
        val weeks = ChronoUnit.WEEKS.between(birthDate.plusMonths(months.toLong()), today).toInt()
        return "$months Bulan, $weeks Minggu"
    }
}