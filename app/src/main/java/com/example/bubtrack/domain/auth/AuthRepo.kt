package com.example.bubtrack.domain.auth

import com.example.bubtrack.utill.Resource
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
}