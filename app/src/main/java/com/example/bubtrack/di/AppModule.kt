package com.example.bubtrack.di

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth() = Firebase.auth

    @Provides
    @Singleton
    fun provideFirestore() = Firebase.firestore
}