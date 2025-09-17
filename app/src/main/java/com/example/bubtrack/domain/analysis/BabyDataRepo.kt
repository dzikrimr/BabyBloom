package com.example.bubtrack.domain.analysis

import com.example.bubtrack.domain.activities.Activity
import com.example.bubtrack.domain.diary.Diary
import com.example.bubtrack.domain.growth.BabyGrowth
import com.example.bubtrack.domain.profile.BabyProfile
import com.google.firebase.auth.FirebaseAuth
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
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    suspend fun getAllUserBabyData(): UserBabyData {
        return try {
            val userId = auth.currentUser?.uid ?: return emptyUserData()

            val activitiesTask = firestore.collection("users")
                .document(userId)
                .collection("activities")
                .get()
                .await()

            val diariesTask = firestore.collection("users")
                .document(userId)
                .collection("diaries")
                .get()
                .await()

            val babyProfilesTask = firestore.collection("users")
                .document(userId)
                .collection("babyProfiles")
                .get()
                .await()

            val growthRecordsTask = firestore.collection("users")
                .document(userId)
                .collection("growthRecords")
                .get()
                .await()

            val activities = activitiesTask.documents.mapNotNull { doc ->
                try {
                    Activity(
                        id = doc.getLong("id")?.toInt() ?: 0,
                        userId = userId,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        date = doc.getLong("date") ?: 0L,
                        hour = doc.getLong("hour")?.toInt() ?: 0,
                        minute = doc.getLong("minute")?.toInt() ?: 0,
                        type = doc.getString("type") ?: ""
                    )
                } catch (_: Exception) {
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
                } catch (_: Exception) {
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
                } catch (_: Exception) {
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
                } catch (_: Exception) {
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
            emptyUserData()
        }
    }

    private fun emptyUserData() = UserBabyData(
        activities = emptyList(),
        diaries = emptyList(),
        babyProfiles = emptyList(),
        growthRecords = emptyList()
    )
}
