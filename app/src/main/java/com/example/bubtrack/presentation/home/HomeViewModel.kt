package com.example.bubtrack.presentation.home

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bubtrack.R
import com.example.bubtrack.data.home.HomeRepoImpl
import com.example.bubtrack.domain.activities.Activity
import com.example.bubtrack.domain.growth.GrowthStats
import com.example.bubtrack.domain.home.HomeRepo
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepo,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        startImageCarousel()
    }

    private fun loadData() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.update { it.copy(errorMessage = "User not authenticated") }
            return
        }

        viewModelScope.launch {
            // Load initial data
            loadBabyProfile(userId)

            // Observe real-time data
            observeGrowthStats(userId)
            observeUpcomingActivities(userId)
        }
    }

    private suspend fun loadBabyProfile(userId: String) {
        repository.getBabyProfile(userId).fold(
            onSuccess = { profile ->
                val age = repository.calculateAge(profile.dateMillis)
                _uiState.update {
                    it.copy(
                        babyProfile = profile,
                        babyAge = age,
                        isLoading = false
                    )
                }
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.message,
                        isLoading = false
                    )
                }
            }
        )
    }

    private fun observeGrowthStats(userId: String) {
        viewModelScope.launch {
            repository.observeGrowthStats(userId).collect { result ->
                result.fold(
                    onSuccess = { stats ->
                        _uiState.update { it.copy(latestGrowthStats = stats) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(errorMessage = error.message) }
                    }
                )
            }
        }
    }

    private fun observeUpcomingActivities(userId: String) {
        viewModelScope.launch {
            repository.observeUpcomingActivities(userId).collect { result ->
                result.fold(
                    onSuccess = { activities ->
                        _uiState.update { it.copy(upcomingActivities = activities) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(errorMessage = error.message) }
                    }
                )
            }
        }
    }

    private fun startImageCarousel() {
        viewModelScope.launch {
            while (true) {
                delay(3000)
                _uiState.update {
                    it.copy(currentImageIndex = (it.currentImageIndex + 1) % 3)
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}