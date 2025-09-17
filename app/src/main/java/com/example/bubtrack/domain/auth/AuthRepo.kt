package com.example.bubtrack.domain.auth

import com.example.bubtrack.presentation.profile.UserProfile
import com.example.bubtrack.utill.Resource
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepo {

    suspend fun loginEmail(email: String, password: String): Flow<Resource<AuthResult>>
    suspend fun registerEmail(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Flow<Resource<AuthResult>>
    suspend fun forgotPassword(email: String): Resource<Unit>
    suspend fun createBabyProfile(
        babyName: String,
        dateMillis: Long,
        selectedGender: String,
        weight: String,
        height: String,
        headCircumference: String,
        armCircumference: String
    ): Resource<Unit>
    suspend fun getUserProfile(): Flow<Resource<UserProfile>>
    suspend fun updateUserProfile(
        name: String,
        babyName: String,
        birthDate: Long,
        gender: String,
        profileImageUrl: String? = null
    ): Resource<Unit>
    suspend fun loginWithGoogle(account: GoogleSignInAccount): Flow<Resource<AuthResult>>
    suspend fun checkOnBoardingStatus(): Resource<Boolean>
    suspend fun getCurrentUser(): Flow<Resource<FirebaseUser>>
    suspend fun logout(): Resource<Unit>
    suspend fun deleteAccount(): Flow<Resource<Unit>> // Add this line
}