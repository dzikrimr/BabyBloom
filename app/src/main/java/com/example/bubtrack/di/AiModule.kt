package com.example.bubtrack.di

import android.content.Context
import dagger.Module
import dagger.Provides
import com.example.bubtrack.data.ai.AudioRepoImpl
import com.example.bubtrack.data.ai.ModelRepoImpl
import com.example.bubtrack.domain.ai.audio.MFCCExtractor
import com.example.bubtrack.domain.usecase.ClassifyAudioUseCase
import com.example.bubtrack.domain.usecase.RecordAudioUseCase
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideMFCCExtractor(): MFCCExtractor = MFCCExtractor()

    @Provides
    @Singleton
    fun provideAudioRepo(
        @ApplicationContext context: Context
    ): AudioRepoImpl = AudioRepoImpl(context) { true }

    @Provides
    @Singleton
    fun provideModelRepo(
        @ApplicationContext context: Context,
        extractor: MFCCExtractor
    ): ModelRepoImpl = ModelRepoImpl(context, extractor)

    @Provides
    fun provideRecordAudioUseCase(
        audioRepo: AudioRepoImpl
    ): RecordAudioUseCase = RecordAudioUseCase(audioRepo)

    @Provides
    fun provideClassifyAudioUseCase(
        modelRepo: ModelRepoImpl
    ): ClassifyAudioUseCase = ClassifyAudioUseCase(modelRepo)
}