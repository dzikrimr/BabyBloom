package com.example.bubtrack.data.auth

import com.example.bubtrack.domain.auth.AuthRepo
import com.example.bubtrack.utill.Resource
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepoImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepo {

    override suspend fun loginEmail(email: String, password: String): Flow<Resource<AuthResult>> = flow {
        emit(Resource.Loading())
        val result = try {
            auth.signInWithEmailAndPassword(email, password).await()
        } catch (e: Exception) {
            throw e // Re-throw to be caught by .catch
        }
        emit(Resource.Success(result))
    }.catch { e ->
        val message = when (e) {
            is FirebaseAuthException -> when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> "Invalid email!"
                "ERROR_WRONG_PASSWORD" -> "Wrong password!"
                "ERROR_USER_NOT_FOUND" -> "User not found!"
                else -> "Login failed: ${e.message}"
            }
            else -> "An unexpected error occurred: ${e.message}"
        }
        emit(Resource.Error(message))
    }

    override suspend fun registerEmail(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Flow<Resource<AuthResult>> = flow {
        emit(Resource.Loading())
        if (password != confirmPassword) {
            emit(Resource.Error("Passwords do not match!"))
            return@flow
        }
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid
            if (userId != null) {
                val userData = hashMapOf(
                    "name" to name,
                    "email" to email
                )
                firestore.collection("users").document(userId)
                    .set(userData)
                    .await()
            }
            emit(Resource.Success(result))
        } catch (e: FirebaseAuthException) {
            val message = when (e.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Email already in use!"
                "ERROR_INVALID_EMAIL" -> "Invalid email!"
                "ERROR_WEAK_PASSWORD" -> "Weak password!"
                else -> "Registration failed: ${e.message}"
            }
            emit(Resource.Error(message))
        } catch (e: Exception) {
            emit(Resource.Error("An error occurred: ${e.message}"))
        }
    }

    override suspend fun forgotPassword(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).result
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage)
        }
    }

    override suspend fun createBabyProfile(
        babyName: String,
        dateMillis: Long,
        selectedGender: String,
        weight: String,
        height: String,
        headCircumference: String,
        armCircumference: String
    ): Resource<Unit> {
        val userId = auth.currentUser?.uid ?: return Resource.Error("Pengguna tidak ditemukan!")
        val babyProfile: Map<String, Any> = mapOf(
            "babyName" to babyName,
            "birthDate" to dateMillis,
            "gender" to selectedGender,
            "weight" to weight,
            "height" to height,
            "headCircumference" to headCircumference,
            "armCircumference" to armCircumference,
            "createdAt" to System.currentTimeMillis()
        )
        return try {
            firestore.collection("users").document(userId)
                .collection("babyProfiles").document("primary")
                .set(babyProfile)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Gagal menyimpan profil: ${e.message}")
        }
    }

    override suspend fun checkOnBoardingStatus(): Resource<Boolean> {
        val userId = auth.currentUser?.uid ?: return Resource.Error("Pengguna tidak ditemukan!")
        try {
            val data = firestore.collection("users").document(userId)
                .collection("babyProfiles").document("primary")
                .get().await()
            return Resource.Success(data.exists())
        } catch (e: Exception) {
            return Resource.Error("${e.message}")
        }
    }

    override suspend fun loginWithGoogle(account: GoogleSignInAccount): Flow<Resource<AuthResult>> = flow {
        emit(Resource.Loading())
        val result = try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).await()
        } catch (e: Exception) {
            throw e
        }
        emit(Resource.Success(result))
    }.catch { e ->
        emit(Resource.Error("Google Sign-In failed: ${e.message}"))
    }
}