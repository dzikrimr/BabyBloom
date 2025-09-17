package com.example.bubtrack.presentation.diary.helper

sealed class ChartType(val displayName: String, val unit: String) {
    object Weight : ChartType("Berat", "kg")
    object Height : ChartType("Tinggi", "cm")
    object HeadCircumference : ChartType("L. Kepala", "cm")
    object ArmLength : ChartType("L. Lengan", "cm")
}