package com.example.bubtrack.di

import android.content.Context
import com.example.bubtrack.data.cloudinary.CloudinaryManager
import com.example.bubtrack.data.home.HomeRepoImpl
import com.example.bubtrack.data.notification.FcmApi
import com.example.bubtrack.domain.ai.SimpleSleepRepository
import com.example.bubtrack.domain.ai.SleepRepository
import com.example.bubtrack.domain.home.HomeRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindHomeRepo(
        homeRepoImpl: HomeRepoImpl
    ): HomeRepo

    @Binds
    @Singleton
    abstract fun bindSleepRepository(
        simpleSleepRepository: SimpleSleepRepository
    ): SleepRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

        @Provides
        @Singleton
        fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

        @Provides
        @Singleton
        fun provideGson(): Gson = Gson()

        @Provides
        @Singleton
        fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

        @Provides
        @Singleton
        fun provideDatabaseReference(database: FirebaseDatabase): DatabaseReference = database.reference

        @Provides
        @Singleton
        fun provideContext(@ApplicationContext context: Context): Context = context.applicationContext

        @Provides
        @Singleton
        fun provideRetrofit(): Retrofit {
            return Retrofit.Builder()
                .baseUrl("http://34.101.146.105:3000")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        @Provides
        @Singleton
        fun provideFcmApi(retrofit: Retrofit): FcmApi {
            return retrofit.create(FcmApi::class.java)
        }

        @Provides
        @Singleton
        fun provideCloudinaryManager(
            @ApplicationContext context: Context
        ): CloudinaryManager {
            return CloudinaryManager(context)
        }

    }
}