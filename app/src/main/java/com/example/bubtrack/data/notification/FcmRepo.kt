package com.example.bubtrack.data.notification

import com.example.bubtrack.presentation.notification.comps.NotificationItem
import com.example.bubtrack.utill.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject

class FcmRepo @Inject constructor(
    private val fcmApi: FcmApi,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth

) {

    suspend fun sendNotification(payload: FcmPayload): Response<FcmResponse> {
        return fcmApi.sendNotification(payload)
    }

    suspend fun saveNotification(notificationItem: NotificationItem): Resource<Unit> {
        val uid = auth.currentUser?.uid
        try {
            if (uid != null) {
                firestore.collection("users").document(uid)
                    .collection("notifications")
                    .add(notificationItem)
                    .await()
                return Resource.Success(Unit)
            } else {
                return Resource.Error("User not authenticated")
            }
        } catch (e: Exception) {
            return Resource.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun getNotifications(): Resource<List<NotificationItem>> {
        val uid = auth.currentUser?.uid
        try {
            if (uid != null) {
                val notifications = firestore.collection("users").document(uid)
                    .collection("notifications")
                    .get()
                    .await()
                    .toObjects(NotificationItem::class.java)
                return Resource.Success(notifications)
            } else {
                return Resource.Error("User not authenticated")
            }
        } catch (e: Exception) {
            return Resource.Error(e.message ?: "Unknown error occurred")
        }
    }
}

