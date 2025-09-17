package com.example.bubtrack.presentation.activities

import com.example.bubtrack.domain.activities.Activity
import com.example.bubtrack.utill.Utility.toLocalDate
import java.time.LocalDate

data class ActivitiesUiState(
    val isLoading: Boolean = false,
    val isError: String? = null,
    val isEmpty: Boolean = false,
    val allActivities: List<Activity> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now()
) {
    val activitiesForSelectedDate: List<Activity>
        get() = allActivities.filter { it.date.toLocalDate() == selectedDate }
}
