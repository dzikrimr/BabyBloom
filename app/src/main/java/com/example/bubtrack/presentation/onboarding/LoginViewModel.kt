package com.example.bubtrack.presentation.onboarding

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bubtrack.domain.auth.AuthRepo
import com.example.bubtrack.domain.auth.LoginState
import com.example.bubtrack.presentation.navigation.MainRoute
import com.example.bubtrack.presentation.navigation.OnBoardingRoute
import com.example.bubtrack.utill.Resource
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepo: AuthRepo
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.value = _state.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        _state.value = _state.value.copy(password = password)
    }

    fun login(navController: androidx.navigation.NavController) {
        viewModelScope.launch {
            if (_state.value.email.isBlank() || _state.value.password.isBlank()) {
                _state.value = _state.value.copy(errorMessage = "Lengkapi email dan password!")
                return@launch
            }
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val result = authRepo.loginEmail(_state.value.email, _state.value.password)
            result.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isSuccess = true,
                            errorMessage = null
                        )
                        navController.navigate(MainRoute) {
                            popUpTo(OnBoardingRoute) { // Use OnBoardingRoute as the root
                                inclusive = true
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = resource.msg
                        )
                    }

                    is Resource.Idle -> TODO()
                }
            }
        }
    }

    fun loginWithGoogle(activity: Activity, navController: androidx.navigation.NavController) {
        viewModelScope.launch {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("YOUR_WEB_CLIENT_ID") // Replace with your Web Client ID
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(activity, gso)
            val signInIntent = googleSignInClient.signInIntent
            // The launcher will handle the result; this is just setup
        }
    }
}