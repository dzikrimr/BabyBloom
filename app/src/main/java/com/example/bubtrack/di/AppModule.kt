package com.example.bubtrack.di

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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


    @Provides
    @Singleton
    fun provideGson() = Gson()

    @Singleton
    @Provides
    fun provideDatabaseInstansnce() : FirebaseDatabase = FirebaseDatabase.getInstance()

    @Singleton
    @Provides
    fun provideDatabaseReference(
        database : FirebaseDatabase
    ) : DatabaseReference = database.reference

    @Provides
    @Singleton
    fun provideContext(
        @ApplicationContext context : Context
    ) : Context = context.applicationContext
}