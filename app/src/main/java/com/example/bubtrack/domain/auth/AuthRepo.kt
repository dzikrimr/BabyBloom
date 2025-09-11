package com.example.bubtrack.domain.auth

import com.example.bubtrack.utill.Resource
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.flow.Flow

interface AuthRepo {

    suspend fun loginEmail (email:String, password:String) : Flow<Resource<AuthResult>>
    suspend fun registerEmail (
        name : String,
        email: String,
        password: String,
        confirmPassword: String
    ) : Flow<Resource<AuthResult>>
    suspend fun forgotPassword (email: String) : Resource<Unit>

    suspend fun createBabyProfile(
        babyName: String,
        dateMillis: Long,
        selectedGender: String,
        weight: String,
        height: String,
        headCircumference: String,
        armCircumference: String
    ): Resource<Unit>
    suspend fun loginWithGoogle(account: GoogleSignInAccount): Flow<Resource<AuthResult>>
    suspend fun checkOnBoardingStatus(): Resource<Boolean>
}