package com.example.bubtrack.data.notification

import retrofit2.Response
import javax.inject.Inject

class FcmRepo @Inject constructor(
    private val fcmApi: FcmApi
) {

    suspend fun sendNotification(payload: FcmPayload) : Response<FcmResponse>{
        return fcmApi.sendNotification(payload)
    }
}