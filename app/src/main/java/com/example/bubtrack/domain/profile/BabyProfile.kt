package com.example.bubtrack.domain.profile

data class BabyProfile(
    val babyName: String,
    val dateMillis: Long,
    val selectedGender: String,
    val weight: String,
    val height: String,
    val headCircumference: String,
    val armCircumference: String
)