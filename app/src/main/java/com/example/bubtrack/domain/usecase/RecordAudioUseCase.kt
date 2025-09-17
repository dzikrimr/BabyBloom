package com.example.bubtrack.domain.usecase

import com.example.bubtrack.data.ai.AudioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecordAudioUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(): ShortArray? = withContext(Dispatchers.IO) {
        audioRepository.recordAudio()
    }
}