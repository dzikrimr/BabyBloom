package com.example.bubtrack.presentation.onboarding.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bubtrack.domain.auth.AuthRepo
import com.example.bubtrack.utill.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepo: AuthRepo
) : ViewModel() {

    private var _loginState = MutableStateFlow(LoginUiState())
    val loginState = _loginState.asStateFlow()


    fun loginWithEmailPassword(email: String, password: String) {
        viewModelScope.launch {
            authRepo.loginEmail(email, password).collect {
                when (it) {
                    is Resource.Loading -> {
                        _loginState.value.copy(
                            isLoading = true
                        )
                    }
                    is Resource.Error -> {
                        _loginState.value.copy(
                            isLoading = false,
                            isSuccess = false,
                            errorMessage = it.msg
                        )
                    }
                    is Resource.Success -> {
                        _loginState.value.copy(
                            isLoading = false,
                            isSuccess = true,
                            errorMessage = null
                        )
                    }
                    else -> {

                    }
                }
            }

        }
    }
}