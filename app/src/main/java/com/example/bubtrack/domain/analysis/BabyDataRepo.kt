package com.example.bubtrack.domain.analysis

import com.example.bubtrack.domain.activities.Activity
import com.example.bubtrack.domain.diary.Diary
import com.example.bubtrack.domain.growth.BabyGrowth
import com.example.bubtrack.domain.profile.BabyProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class UserBabyData(
    val activities: List<Activity>,
    val diaries: List<Diary>,
    val babyProfiles: List<BabyProfile>,
    val growthRecords: List<BabyGrowth>
)

@Singleton
class BabyDataRepo @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getAllUserBabyData(userId: String): UserBabyData {
        return try {
            val docRef = firestore.collection("users").document(userId)
            val activitiesTask = docRef.collection("activities")
                .get()
                .await()

            val diariesTask = docRef.collection("diaries")
                .get()
                .await()

            val babyProfilesTask = docRef.collection("babyProfiles")
                .get()
                .await()

            val growthRecordsTask = docRef.collection("growthRecords")
                .get()
                .await()

            val activities = activitiesTask.documents.mapNotNull { doc ->
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
            }

            val diaries = diariesTask.documents.mapNotNull { doc ->
                try {
                    Diary(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        desc = doc.getString("desc") ?: "",
                        date = doc.getLong("date") ?: 0L,
                        imgUrl = doc.getString("imgUrl")
                    )
                } catch (e: Exception) {
                    null
                }
            }

            val babyProfiles = babyProfilesTask.documents.mapNotNull { doc ->
                try {
                    BabyProfile(
                        babyName = doc.getString("babyName") ?: "",
                        dateMillis = doc.getLong("dateMillis") ?: 0L,
                        selectedGender = doc.getString("selectedGender") ?: "",
                        weight = doc.getString("weight") ?: "",
                        height = doc.getString("height") ?: "",
                        headCircumference = doc.getString("headCircumference") ?: "",
                        armCircumference = doc.getString("armCircumference") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }

            val growthRecords = growthRecordsTask.documents.mapNotNull { doc ->
                try {
                    BabyGrowth(
                        id = doc.id,
                        date = doc.getLong("date") ?: 0L,
                        weight = doc.getDouble("weight"),
                        height = doc.getDouble("height"),
                        headCircumference = doc.getDouble("headCircumference"),
                        armLength = doc.getDouble("armLength"),
                        ageInMonths = doc.getLong("ageInMonths")?.toInt() ?: 0
                    )
                } catch (e: Exception) {
                    null
                }
            }

            UserBabyData(
                activities = activities,
                diaries = diaries,
                babyProfiles = babyProfiles,
                growthRecords = growthRecords
            )
        } catch (e: Exception) {
            UserBabyData(
                activities = emptyList(),
                diaries = emptyList(),
                babyProfiles = emptyList(),
                growthRecords = emptyList()
            )
        }
    }
}