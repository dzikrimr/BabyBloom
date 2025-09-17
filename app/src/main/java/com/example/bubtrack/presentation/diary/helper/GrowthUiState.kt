package com.example.bubtrack.presentation.diary.helper

import com.example.bubtrack.domain.growth.BabyGrowth

data class GrowthUiState(
    val isLoading : Boolean = false,
    val isError : String? = null,
    val isEmpty : Boolean = false,
    val isSuccess : List<BabyGrowth> = emptyList()
)