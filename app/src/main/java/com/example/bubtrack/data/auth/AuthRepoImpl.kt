package com.example.bubtrack.data.auth

import android.widget.Toast
import com.example.bubtrack.domain.auth.AuthRepo
import com.example.bubtrack.presentation.navigation.CreateProfileRoute
import com.example.bubtrack.utill.Resource
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepoImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepo {

    override suspend fun loginEmail(
        email: String,
        password: String
    ): Flow<Resource<AuthResult>> {
        return flow {
            emit(Resource.Loading())
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                emit(Resource.Success(result))
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Unknown error occurred"))
            }
        }
    }

    override suspend fun registerEmail(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Flow<Resource<AuthResult>> {
        return flow {
            emit(Resource.Loading())
            try {
                val data = auth.createUserWithEmailAndPassword(email,password).await()
                data?.let {
                    val userData = hashMapOf(
                        "name" to name,
                        "email" to email
                    )
                    firestore.collection("users").document(data.user!!.uid)
                        .set(userData)
                        .await()
                    emit(Resource.Success(data))
                }
            } catch (e: Exception){
                emit(Resource.Error(e.message ?: "Unknown error occurred"))
            }
        }
    }

    override suspend fun forgotPassword(email: String): Resource<Unit> {
        TODO("Not yet implemented")
    }
}