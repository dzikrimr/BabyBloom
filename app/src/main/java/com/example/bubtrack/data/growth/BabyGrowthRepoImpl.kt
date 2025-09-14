package com.example.bubtrack.data.growth

import com.example.bubtrack.domain.growth.BabyGrowth
import com.example.bubtrack.domain.growth.BabyGrowthRepo
import com.example.bubtrack.domain.growth.GrowthStats
import com.example.bubtrack.utill.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import javax.inject.Inject

class BabyGrowthRepoImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : BabyGrowthRepo {
    override suspend fun getStats(): Flow<Resource<GrowthStats>> {
        val uid = auth.currentUser?.uid
        return flow {
            emit(Resource.Loading())
            try {
                if (uid == null) {
                    emit(Resource.Error("User not authenticated"))
                    return@flow
                }
                val snapshot = firestore.collection("users")
                    .document(uid)
                    .collection("babyProfiles")
                    .document("primary")
                    .get()
                    .await()

                val babyWeight = snapshot.getDouble("weight")
                val babyHeight = snapshot.getDouble("height")
                val headCircumference = snapshot.getDouble("headCircumference")
                val armCircumference = snapshot.getDouble("armCircumference")

                val growthStats = GrowthStats(
                    weight = babyWeight ?: 0.0,
                    height = babyHeight ?: 0.0,
                    headCircum = headCircumference ?: 0.0,
                    armCircum = armCircumference ?: 0.0
                )
                emit(Resource.Success(growthStats))
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Unknown error"))
            }
        }
    }

    override suspend fun updateStats(growthStats: GrowthStats): Flow<Resource<Boolean>> {
        TODO("Not yet implemented")
    }

    override suspend fun getBabyGrowth(): Flow<Resource<List<BabyGrowth>>> {
        val calendar = Calendar.getInstance()
        return flow {
            emit(Resource.Loading())

            // Scenario 1: Empty data (comment/uncomment to test different scenarios)
             val data = emptyList<BabyGrowth>()

            // Scenario 2: Single data point
//             val data =  listOf(
//                 BabyGrowth(
//                     id = "1",
//                    date = System.currentTimeMillis(),
//                    weight = 3.2,
//                    height = 52.0,
//                    headCircumference = 35.0,
//                    armLength = 18.0,
//                    ageInMonths = 0
//                 )
//             )

            // Scenario 3
//            val data =  listOf(
//                BabyGrowth(
//                    id = "1",
//                    date = System.currentTimeMillis(),
//                    weight = 3.2,
//                    height = 52.0,
//                    headCircumference = 35.0,
//                    armLength = 18.0,
//                    ageInMonths = 0
//                ),
//                BabyGrowth(
//                    id = "2",
//                    date = System.currentTimeMillis(),
//                    weight = 4.1,
//                    height = 55.0,
//                    headCircumference = 36.0,
//                    armLength = 20.0,
//                    ageInMonths = 1
//                ),
//                BabyGrowth(
//                    id = "3",
//                    date = System.currentTimeMillis(),
//                    weight = 5.3,
//                    height = null, // Some missing data
//                    headCircumference = 37.0,
//                    armLength = null,
//                    ageInMonths = 2
//                )
//            )
            emit(Resource.Success(data))
        }
    }
}