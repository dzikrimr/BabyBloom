package com.example.bubtrack.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bubtrack.domain.auth.AuthRepo
import com.example.bubtrack.domain.auth.RegisterState
import com.example.bubtrack.utill.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepo: AuthRepo
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    fun onNameChange(name: String) {
        _state.value = _state.value.copy(name = name)
    }

    fun onEmailChange(email: String) {
        _state.value = _state.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        _state.value = _state.value.copy(password = password)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.value = _state.value.copy(confirmPassword = confirmPassword)
    }

    fun register(navController: androidx.navigation.NavController) {
        viewModelScope.launch {
            if (_state.value.name.isBlank() || _state.value.email.isBlank() ||
                _state.value.password.isBlank() || _state.value.confirmPassword.isBlank()
            ) {
                _state.value = _state.value.copy(errorMessage = "Lengkapi semua field!")
                return@launch
            }
            if (_state.value.password != _state.value.confirmPassword) {
                _state.value = _state.value.copy(errorMessage = "Password tidak cocok!")
                return@launch
            }
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val result = authRepo.registerEmail(
                name = _state.value.name,
                email = _state.value.email,
                password = _state.value.password,
                confirmPassword = _state.value.confirmPassword
            )
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
                        navController.navigate("create_profile") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
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
}