package com.example.bubtrack.presentation.home

import com.example.bubtrack.domain.activities.Activity
import com.example.bubtrack.domain.growth.GrowthStats
import com.example.bubtrack.domain.profile.BabyProfile

data class HomeUiState(
    val isLoading: Boolean = true,
    val babyProfile: BabyProfile? = null,
    val babyAge: String = "",
    val latestGrowthStats: GrowthStats = GrowthStats(),
    val upcomingActivities: List<Activity> = emptyList(),
    val errorMessage: String? = null,
    val currentImageIndex: Int = 0
)