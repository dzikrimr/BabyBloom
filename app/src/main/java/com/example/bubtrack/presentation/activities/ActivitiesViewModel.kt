package com.example.bubtrack.presentation.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bubtrack.domain.activities.ActivitiesRepo
import com.example.bubtrack.domain.activities.Activity
import com.example.bubtrack.utill.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    private val activitiesRepo: ActivitiesRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivitiesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getAllActivities()
    }

    private fun getAllActivities() {
        viewModelScope.launch {
            activitiesRepo.getAllActivities().collect { res ->
                when (res) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }

                    is Resource.Success -> {
                        val data = res.data.orEmpty()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isEmpty = data.isEmpty(),
                                isError = null,
                                allActivities = data
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isError = res.msg,
                                allActivities = emptyList()
                            )
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }
}
