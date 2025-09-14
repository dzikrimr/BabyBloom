package com.example.bubtrack

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bubtrack.data.webrtc.FirebaseClient
import com.example.bubtrack.domain.auth.AuthRepo
import com.example.bubtrack.presentation.navigation.AppRoute
import com.example.bubtrack.presentation.navigation.MainRoute
import com.example.bubtrack.presentation.navigation.OnBoardingRoute
import com.example.bubtrack.utill.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepo: AuthRepo
) : ViewModel() {

    var splashCondition by mutableStateOf(true)
        private set

    private val _startDestination = MutableStateFlow<AppRoute>(OnBoardingRoute)
    val startDestination: StateFlow<AppRoute> = _startDestination

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            authRepo.getCurrentUser().collect { result ->
                if (result is Resource.Success) {
                    val onboarding = authRepo.checkOnBoardingStatus().data
                    _startDestination.value = if (onboarding == true) MainRoute else OnBoardingRoute
                } else {
                    _startDestination.value = OnBoardingRoute
                }
            }
        }
    }

    fun completeSplash() {
        splashCondition = false
    }
}