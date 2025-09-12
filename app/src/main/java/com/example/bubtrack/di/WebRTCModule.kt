package com.example.bubtrack.di

import android.content.Context
import com.example.bubtrack.data.webrtc.WebRTCService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebRTCModule {

    @Provides
    @Singleton
    fun provideWebRTCService(
        @ApplicationContext context: Context
    ): WebRTCService {
        return WebRTCService(context)
    }
}