package com.example.bubtrack.data.notification

data class FcmPayload(
    val token: String,
    val title: String,
    val body: String
)
