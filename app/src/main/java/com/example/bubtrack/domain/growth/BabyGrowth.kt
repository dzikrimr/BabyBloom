package com.example.bubtrack.domain.growth

data class BabyGrowth(
    val id: String = "",
    val date: Long,
    val weight: Double? = null,
    val height: Double? = null,
    val headCircumference: Double? = null,
    val armLength: Double? = null,
    val ageInMonths: Int = 0
)
