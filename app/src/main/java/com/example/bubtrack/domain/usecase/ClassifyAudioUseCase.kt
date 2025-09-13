package com.example.bubtrack.domain.usecase

import com.example.bubtrack.data.ai.ModelRepository

class ClassifyAudioUseCase(
    private val modelRepository: ModelRepository
) {
    suspend fun initialize() {
        modelRepository.loadModel()
    }

    suspend operator fun invoke(audioData: ShortArray): Pair<String, List<Pair<String, Float>>> {
        return modelRepository.classifyAudio(audioData)
    }

    fun release() {
        modelRepository.release()
    }
}