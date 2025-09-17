package com.example.bubtrack.presentation.ai.growthanalysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bubtrack.data.analysis.BabyAnalysisResult
import com.example.bubtrack.data.analysis.GeminiAiService
import com.example.bubtrack.domain.analysis.BabyDataRepo
import com.example.bubtrack.domain.analysis.UserBabyData
import com.example.bubtrack.presentation.ai.growthanalysis.comps.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GrowthAnalysisUiState(
    val isLoading: Boolean = false,
    val isReady: Boolean = false, // tambahkan
    val babyData: UserBabyData? = null,
    val analysisResult: BabyAnalysisResult? = null,
    val chatMessages: List<ChatMessage> = emptyList(),
    val selectedPeriod: String = "7 Hari Terakhir",
    val error: String? = null,
    val isAnalysisGenerated: Boolean = false,
    val isChatLoading: Boolean = false
)

@HiltViewModel
class GrowthAnalysisViewModel @Inject constructor(
    private val babyDataRepository: BabyDataRepo,
    private val geminiAiService: GeminiAiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(GrowthAnalysisUiState())
    val uiState: StateFlow<GrowthAnalysisUiState> = _uiState.asStateFlow()

    private var userId: String = "" // This should be set from auth service
    private var babyName: String = "your baby"

    fun initialize(userId: String) {
        this.userId = userId
        loadBabyData()
    }

    fun updateSelectedPeriod(period: String) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        // Optionally filter data based on period
        generateAnalysis()
    }

    private fun loadBabyData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val babyData = babyDataRepository.getAllUserBabyData(userId)

                // Extract baby name from profile if available
                if (babyData.babyProfiles.isNotEmpty()) {
                    babyName = babyData.babyProfiles.first().babyName
                }

                _uiState.value = _uiState.value.copy(
                    babyData = babyData,
                    isLoading = false
                )

                // Auto-generate analysis after data is loaded
                generateAnalysis()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load baby data: ${e.message}"
                )
            }
        }
    }

    fun generateAnalysis() {
        val currentBabyData = _uiState.value.babyData ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Filter data based on selected period if needed
                val filteredData = filterDataByPeriod(currentBabyData, _uiState.value.selectedPeriod)

                val analysisResult = geminiAiService.generateBabyAnalysis(
                    babyData = filteredData,
                    babyName = babyName
                )

                _uiState.value = _uiState.value.copy(
                    analysisResult = analysisResult,
                    isLoading = false,
                    isAnalysisGenerated = true,
                    isReady = true
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to generate analysis: ${e.message}"
                )
            }
        }
    }

    fun sendChatMessage(message: String) {
        val currentBabyData = _uiState.value.babyData ?: return

        // Add user message immediately
        val userMessage = ChatMessage(text = message, isFromUser = true)
        val currentMessages = _uiState.value.chatMessages + userMessage

        _uiState.value = _uiState.value.copy(
            chatMessages = currentMessages,
            isChatLoading = true
        )

        viewModelScope.launch {
            try {
                // Prepare chat history for context
                val chatHistory = currentMessages.takeLast(6).map { msg ->
                    if (msg.isFromUser) "User: ${msg.text}" else "AI: ${msg.text}"
                }

                val aiResponse = geminiAiService.chatWithAi(
                    message = message,
                    babyData = currentBabyData,
                    chatHistory = chatHistory
                )

                val aiMessage = ChatMessage(text = aiResponse, isFromUser = false)

                _uiState.value = _uiState.value.copy(
                    chatMessages = currentMessages + aiMessage,
                    isChatLoading = false
                )

            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    text = "I'm having trouble responding right now. Please try again later.",
                    isFromUser = false
                )

                _uiState.value = _uiState.value.copy(
                    chatMessages = currentMessages + errorMessage,
                    isChatLoading = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetChat() {
        _uiState.value = _uiState.value.copy(chatMessages = emptyList())
    }

    private fun filterDataByPeriod(data: UserBabyData, period: String): UserBabyData {
        val daysToFilter = when (period) {
            "7 Hari Terakhir" -> 7
            "14 Hari Terakhir" -> 14
            "30 Hari Terakhir" -> 30
            else -> 30
        }

        val cutoffTime = System.currentTimeMillis() - (daysToFilter * 24 * 60 * 60 * 1000L)

        return data.copy(
            activities = data.activities.filter { it.date >= cutoffTime },
            diaries = data.diaries.filter { it.date >= cutoffTime },
            growthRecords = data.growthRecords.filter { it.date >= cutoffTime }
            // Keep all baby profiles as they don't have date filtering
        )
    }

    fun refreshData() {
        loadBabyData()
    }
}