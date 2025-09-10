package com.example.bubtrack.presentation.onboarding.login

import com.example.bubtrack.presentation.navigation.AppRoute

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val navDestination: AppRoute? = null
)
