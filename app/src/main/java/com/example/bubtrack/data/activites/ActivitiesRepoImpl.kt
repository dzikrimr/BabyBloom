package com.example.bubtrack.data.activites

import com.example.bubtrack.domain.activities.ActivitiesRepo
import com.example.bubtrack.domain.activities.Activity
import com.example.bubtrack.utill.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ActivitiesRepoImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ActivitiesRepo {

    override suspend fun getAllActivities(): Flow<Resource<List<Activity>>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("activities")
                .get()
                .await()
            val activities = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Activity::class.java)?.copy(id = doc.id.hashCode()) // Use document ID hash as ID
            }
            emit(Resource.Success(activities))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load activities"))
        }
    }

    override suspend fun addActivity(activity: Activity): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val activityRef = firestore.collection("users")
                .document(userId)
                .collection("activities")
                .add(activity)
                .await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to add activity"))
        }
    }
}