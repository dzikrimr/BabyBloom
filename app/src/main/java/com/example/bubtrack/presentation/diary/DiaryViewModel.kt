package com.example.bubtrack.presentation.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bubtrack.domain.growth.BabyGrowthRepo
import com.example.bubtrack.presentation.diary.helper.GrowthUiState
import com.example.bubtrack.utill.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val growthRepo: BabyGrowthRepo
) : ViewModel(){

    private val _uiState = MutableStateFlow(GrowthUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getGrowth()
    }

    private fun getGrowth(){
        viewModelScope.launch {
            growthRepo.getBabyGrowth().collect {
                when(it){
                    is Resource.Loading -> {
                        _uiState.value = GrowthUiState(isLoading = true)
                    }
                    is Resource.Success -> {
                        _uiState.value = GrowthUiState(isSuccess = it.data!!)
                    }
                    is Resource.Error -> {
                        _uiState.value = GrowthUiState(isError = "error")
                    }
                    else -> {}
                }
            }
        }
    }


}