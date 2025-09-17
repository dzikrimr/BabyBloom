package com.example.bubtrack.presentation.ai.cryanalyzer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bubtrack.domain.usecase.ClassifyAudioUseCase
import com.example.bubtrack.domain.usecase.RecordAudioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoiceAnalyzerState(
    val isRecording: Boolean = false,
    val classificationResult: String = "Tekan tombol untuk merekam",
    val confidenceScores: List<Pair<String, Float>> = listOf(
        "hungry" to 0f,
        "cold_hot" to 0f,
        "tired" to 0f,
        "belly_pain" to 0f,
        "discomfort" to 0f,
        "burping" to 0f,
        "scared" to 0f,
        "unknown" to 0f
    )
)

sealed class VoiceAnalyzerEvent {
    object ToggleRecording : VoiceAnalyzerEvent()
    object PermissionDenied : VoiceAnalyzerEvent()
}

@HiltViewModel
class CryAnalyzerViewModel @Inject constructor(
    private val recordAudioUseCase: RecordAudioUseCase,
    private val classifyAudioUseCase: ClassifyAudioUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(VoiceAnalyzerState())
    val state: StateFlow<VoiceAnalyzerState> = _state.asStateFlow()

    private val defaultNeeds =  listOf(
        "hungry" to 0f,
        "cold_hot" to 0f,
        "tired" to 0f,
        "belly_pain" to 0f,
        "discomfort" to 0f,
        "burping" to 0f,
        "scared" to 0f,
        "unknown" to 0f
    )

    // Define sampleRate as a constant
    private val sampleRate = 22050 // Matches AudioRepoImpl

    init {
        // Initialize model loading if needed
        viewModelScope.launch {
            classifyAudioUseCase.initialize()
        }
    }

    fun onEvent(event: VoiceAnalyzerEvent) {
        when (event) {
            is VoiceAnalyzerEvent.ToggleRecording -> toggleRecording()
            is VoiceAnalyzerEvent.PermissionDenied -> {
                _state.value = _state.value.copy(
                    classificationResult = "Izin rekam audio diperlukan."
                )
            }
        }
    }

    private fun toggleRecording() {
        val isStarting = !_state.value.isRecording
        _state.value = _state.value.copy(
            isRecording = isStarting,
            classificationResult = if (isStarting) "Merekam..." else "Rekaman dihentikan.",
            confidenceScores = if (isStarting) defaultNeeds else _state.value.confidenceScores
        )
        if (isStarting) {
            viewModelScope.launch {
                try {
                    val audioData = recordAudioUseCase()
                    if (audioData != null && audioData.size >= sampleRate / 2) {
                        val (label, scores) = classifyAudioUseCase(audioData)
                        _state.value = _state.value.copy(
                            classificationResult = "Hasil: $label",
                            confidenceScores = scores,
                            isRecording = false
                        )
                    } else {
                        _state.value = _state.value.copy(
                            classificationResult = "Gagal merekam audio (terlalu pendek atau error).",
                            isRecording = false
                        )
                    }
                } catch (e: Exception) {
                    _state.value = _state.value.copy(
                        classificationResult = "Error klasifikasi: ${e.message}",
                        isRecording = false
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        classifyAudioUseCase.release()
    }
}