package com.example.bubtrack.presentation.diary.helper

import com.example.bubtrack.domain.growth.BabyGrowthModel

data class GrowthUiState(
    val isLoading : Boolean = false,
    val isError : String? = null,
    val isEmpty : Boolean = false,
    val isSuccess : List<BabyGrowthModel> = emptyList()
)