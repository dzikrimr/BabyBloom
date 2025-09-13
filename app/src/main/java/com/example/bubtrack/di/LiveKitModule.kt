package com.example.bubtrack.di

import android.content.Context
import com.example.bubtrack.data.livekit.LiveKitService
import com.example.bubtrack.data.livekit.TokenGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LiveKitModule {

    @Provides
    @Singleton
    fun provideTokenGenerator(): TokenGenerator {
        return TokenGenerator()
    }

    @Provides
    @Singleton
    fun provideLiveKitService(
        @ApplicationContext context: Context,
        tokenGenerator: TokenGenerator
    ): LiveKitService {
        return LiveKitService(context, tokenGenerator)
    }
}