package com.example.bubtrack.presentation.onboarding.register

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
class RegisterViewModel @Inject constructor(
    private val authRepo: AuthRepo
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state = _state.asStateFlow()

    fun onNameChange(name: String) = updateState { copy(name = name) }
    fun onEmailChange(email: String) = updateState { copy(email = email) }
    fun onPasswordChange(password: String) = updateState { copy(password = password) }
    fun onConfirmPasswordChange(confirmPassword: String) = updateState { copy(confirmPassword = confirmPassword) }

    fun register() {
        viewModelScope.launch {
            val current = _state.value
            if (current.name.isBlank() || current.email.isBlank() ||
                current.password.isBlank() || current.confirmPassword.isBlank()
            ) {
                updateState { copy(errorMessage = "Lengkapi semua field!") }
                return@launch
            }
            if (current.password != current.confirmPassword) {
                updateState { copy(errorMessage = "Password tidak cocok!") }
                return@launch
            }

            authRepo.registerEmail(
                name = current.name,
                email = current.email,
                password = current.password,
                confirmPassword = current.confirmPassword
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> updateState { copy(isLoading = true) }
                    is Resource.Success -> updateState {
                        copy(isLoading = false, isSuccess = true, errorMessage = null)
                    }
                    is Resource.Error -> updateState {
                        copy(isLoading = false, errorMessage = resource.msg)
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun updateState(reducer: RegisterUiState.() -> RegisterUiState) {
        _state.value = _state.value.reducer()
    }
}
