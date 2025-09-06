package com.example.bubtrack.domain.growth

import java.util.Date

data class BabyGrowthModel(
    val id: String = "",
    val date: Long,
    val weight: Double? = null,
    val height: Double? = null,
    val headCircumference: Double? = null,
    val armLength: Double? = null,
    val ageInMonths: Int = 0
)
