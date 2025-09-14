package com.example.bubtrack.presentation.onboarding.login

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bubtrack.domain.auth.AuthRepo
import com.example.bubtrack.presentation.navigation.AppRoute
import com.example.bubtrack.presentation.navigation.CreateProfileRoute
import com.example.bubtrack.presentation.navigation.LoginRoute
import com.example.bubtrack.presentation.navigation.MainRoute
import com.example.bubtrack.utill.Resource
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
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
    var navDestination by mutableStateOf<AppRoute>(LoginRoute)

    fun loginWithEmailPassword(email: String, password: String) {
        viewModelScope.launch {
            authRepo.loginEmail(email, password).collect {
                when (it) {
                    is Resource.Loading -> {
                        _loginState.value = _loginState.value.copy(
                            isLoading = true
                        )
                    }
                    is Resource.Error -> {
                        _loginState.value = _loginState.value.copy(
                            isLoading = false,
                            isSuccess = false,
                            errorMessage = it.msg
                        )
                    }
                    is Resource.Success -> {
                        checkOnBoarding()
                    }
                    else -> {
                    }
                }
            }
        }
    }

    fun loginWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            authRepo.loginWithGoogle(account).collect {
                when (it) {
                    is Resource.Loading -> {
                        _loginState.value = _loginState.value.copy(
                            isLoading = true
                        )
                    }
                    is Resource.Error -> {
                        _loginState.value = _loginState.value.copy(
                            isLoading = false,
                            isSuccess = false,
                            errorMessage = it.msg
                        )
                    }
                    is Resource.Success -> {
                        checkOnBoarding()
                    }
                    else -> {
                    }
                }
            }
        }
    }

    private fun checkOnBoarding() {
        viewModelScope.launch {
            val res = authRepo.checkOnBoardingStatus().data
            val destination = if (res == true) MainRoute else CreateProfileRoute
            _loginState.value = _loginState.value.copy(
                isLoading = false,
                isSuccess = true,
                errorMessage = null,
                navDestination = destination
            )
        }
    }

    fun forgotPassword(email: String){
        viewModelScope.launch {
            authRepo.forgotPassword(email)
        }
    }
}