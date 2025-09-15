package com.example.bubtrack.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bubtrack.data.cloudinary.CloudinaryManager
import com.example.bubtrack.domain.auth.AuthRepo
import com.example.bubtrack.utill.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val babyName: String = "",
    val birthDate: Long = 0L,
    val gender: String = "",
    val profileImageUrl: String = ""
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepo: AuthRepo,
    private val cloudinaryManager: CloudinaryManager
) : ViewModel() {

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uploadState = MutableStateFlow<Resource<String>>(Resource.Idle)
    val uploadState: StateFlow<Resource<String>> = _uploadState.asStateFlow()

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            authRepo.getUserProfile().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _userProfile.value = resource.data ?: UserProfile()
                    }
                    is Resource.Error -> {
                        _userProfile.value = UserProfile()
                    }
                    is Resource.Loading -> {
                    }
                    is Resource.Idle -> {
                        // No action needed for Idle state
                    }
                }
                _isLoading.value = false
            }
        }
    }

    fun uploadProfileImage(uri: Uri) {
        viewModelScope.launch {
            _uploadState.value = Resource.Loading()
            try {
                val result = cloudinaryManager.uploadImage(uri.toString())
                _uploadState.value = when {
                    result.isSuccess -> Resource.Success(result.getOrNull())
                    else -> Resource.Error(result.exceptionOrNull()?.message ?: "Upload failed")
                }
                if (result.isSuccess) {
                    result.getOrNull()?.let { url ->
                        _userProfile.value = _userProfile.value.copy(profileImageUrl = url)
                    }
                }
            } catch (e: Exception) {
                _uploadState.value = Resource.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateProfile(
        name: String,
        babyName: String,
        birthDate: Long,
        gender: String,
        profileImageUrl: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _userProfile.value = UserProfile(
                name = name,
                email = _userProfile.value.email,
                babyName = babyName,
                birthDate = birthDate,
                gender = gender,
                profileImageUrl = profileImageUrl ?: _userProfile.value.profileImageUrl
            )
            authRepo.updateUserProfile(name, babyName, birthDate, gender, profileImageUrl)
            fetchUserProfile()
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepo.logout()
            _userProfile.value = UserProfile()
        }
    }
}