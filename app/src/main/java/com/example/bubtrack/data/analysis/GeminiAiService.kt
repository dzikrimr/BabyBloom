package com.example.bubtrack.data.analysis

import com.example.bubtrack.domain.analysis.UserBabyData
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

data class BabyAnalysisResult(
    val summary: String,
    val insights: List<String>,
    val recommendations: List<String>,
    val concerns: List<String>
)

@Singleton
class GeminiAiService @Inject constructor() {

    companion object {
        private const val API_KEY = "AIzaSyCfJbD_zBeWi3lTTpgEfJXXunWemXPMSTg"
        private const val MODEL_NAME = "gemini-2.0-flash"
    }

    private val generativeModel = GenerativeModel(
        modelName = MODEL_NAME,
        apiKey = API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 8192
        }
    )

    suspend fun generateBabyAnalysis(
        babyData: UserBabyData,
        babyName: String = "your baby"
    ): BabyAnalysisResult {
        return try {
            val prompt = buildAnalysisPrompt(babyData, babyName)

            val response = generativeModel.generateContent(
                content {
                    text(prompt)
                }
            )

            val responseText = response.text ?: "Unable to generate analysis"
            parseAnalysisResponse(responseText)

        } catch (e: Exception) {
            BabyAnalysisResult(
                summary = "Unable to generate analysis at the moment. Please try again later.",
                insights = emptyList(),
                recommendations = listOf("Ensure consistent data recording for better insights"),
                concerns = emptyList()
            )
        }
    }

    suspend fun chatWithAi(
        message: String,
        babyData: UserBabyData,
        chatHistory: List<String> = emptyList()
    ): String {
        return try {
            val contextPrompt = buildChatContextPrompt(babyData, message, chatHistory)

            val response = generativeModel.generateContent(
                content {
                    text(contextPrompt)
                }
            )

            val responseText = response.text ?: "I apologize, I'm having trouble responding right now. Please try again."
            responseText

        } catch (e: Exception) {
            "I'm currently unable to respond. Please check your connection and try again."
        }
    }

    private fun buildAnalysisPrompt(babyData: UserBabyData, babyName: String): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        return buildString {
            appendLine("You are a professional pediatric AI assistant. Analyze the following baby data and provide comprehensive insights in Indonesian.")
            appendLine()
            appendLine("Baby Name: $babyName")
            appendLine()

            // Baby Profile Data
            if (babyData.babyProfiles.isNotEmpty()) {
                appendLine("=== BABY PROFILE ===")
                babyData.babyProfiles.forEach { profile ->
                    appendLine("Name: ${profile.babyName}")
                    appendLine("Birth Date: ${dateFormat.format(Date(profile.dateMillis))}")
                    appendLine("Gender: ${profile.selectedGender}")
                    appendLine("Birth Weight: ${profile.weight} kg")
                    appendLine("Birth Height: ${profile.height} cm")
                    appendLine("Birth Head Circumference: ${profile.headCircumference} cm")
                    appendLine("Birth Arm Circumference: ${profile.armCircumference} cm")
                }
                appendLine()
            }

            // Growth Records Data
            if (babyData.growthRecords.isNotEmpty()) {
                appendLine("=== GROWTH RECORDS ===")
                babyData.growthRecords.sortedBy { it.date }.forEach { record ->
                    appendLine("Date: ${dateFormat.format(Date(record.date))}")
                    appendLine("Age: ${record.ageInMonths} months")
                    record.weight?.let { appendLine("Weight: $it kg") }
                    record.height?.let { appendLine("Height: $it cm") }
                    record.headCircumference?.let { appendLine("Head Circumference: $it cm") }
                    record.armLength?.let { appendLine("Arm Length: $it cm") }
                    appendLine("---")
                }
                appendLine()
            }

            // Activities Data
            if (babyData.activities.isNotEmpty()) {
                appendLine("=== DAILY ACTIVITIES ===")
                val groupedActivities = babyData.activities.groupBy { it.type }
                groupedActivities.forEach { (type, activities) ->
                    appendLine("$type Activities (${activities.size} records):")
                    activities.take(5).forEach { activity ->
                        appendLine("- ${activity.title}: ${activity.description} (${dateFormat.format(Date(activity.date))})")
                    }
                    if (activities.size > 5) {
                        appendLine("... and ${activities.size - 5} more records")
                    }
                    appendLine()
                }
            }

            // Diary Entries Data
            if (babyData.diaries.isNotEmpty()) {
                appendLine("=== DIARY ENTRIES ===")
                babyData.diaries.sortedByDescending { it.date }.take(5).forEach { diary ->
                    appendLine("${dateFormat.format(Date(diary.date))}: ${diary.title}")
                    appendLine("Description: ${diary.desc}")
                    appendLine("---")
                }
                if (babyData.diaries.size > 5) {
                    appendLine("... and ${babyData.diaries.size - 5} more diary entries")
                }
                appendLine()
            }

            appendLine("=== ANALYSIS REQUEST ===")
            appendLine("Provide a comprehensive analysis in Indonesian with the following format:")
            appendLine("SUMMARY:")
            appendLine("Write 2-3 sentences summarizing the baby's overall growth and development, focusing on key trends and milestones.")
            appendLine()
            appendLine("INSIGHTS:")
            appendLine("List 3-5 key insights about the baby's development patterns, each as a concise bullet point starting with '•'.")
            appendLine()
            appendLine("RECOMMENDATIONS:")
            appendLine("List 3-5 specific, actionable recommendations for the parents, each as a concise bullet point starting with '•'.")
            appendLine()
            appendLine("CONCERNS:")
            appendLine("List any areas that need attention or monitoring, each as a concise bullet point starting with '•'. If no concerns exist, state 'Tidak ada kekhawatiran saat ini.'")
            appendLine()
            appendLine("Instructions:")
            appendLine("- Use formal, clear, and engaging Indonesian language, like an informative article.")
            appendLine("- Do not include square brackets '[]', asterisks '**', or placeholder text in the output.")
            appendLine("- Do not use phrases like 'berdasarkan data', 'berikut adalah', or other redundant introductions; directly provide the content.")
            appendLine("- Ensure each section contains actual content based on the provided data, not the example text.")
            appendLine("- Always recommend consulting healthcare professionals for medical concerns.")
            appendLine("- INSIGHTS, RECOMMENDATIONS, and CONCERNS, insert a blank line between each bullet point to improve readability")
        }
    }

    private fun buildChatContextPrompt(
        babyData: UserBabyData,
        userMessage: String,
        chatHistory: List<String>
    ): String {
        return buildString {
            appendLine("You are a knowledgeable pediatric AI assistant helping parents track their baby's development, responding in Indonesian.")
            appendLine("You have access to the baby's complete data including growth records, activities, and diary entries.")
            appendLine()

            appendLine("=== BABY DATA CONTEXT ===")
            if (babyData.babyProfiles.isNotEmpty()) {
                val profile = babyData.babyProfiles.first()
                val ageInMonths = calculateAgeInMonths(profile.dateMillis)
                appendLine("Baby: ${profile.babyName}, ${profile.selectedGender}, $ageInMonths months old")
            }
            if (babyData.growthRecords.isNotEmpty()) {
                val latest = babyData.growthRecords.maxByOrNull { it.date }
                latest?.let {
                    appendLine("Latest measurements: Weight: ${it.weight}kg, Height: ${it.height}cm")
                }
                appendLine("Total growth records: ${babyData.growthRecords.size}")
            }
            appendLine("Daily activities recorded: ${babyData.activities.size}")
            appendLine("Diary entries: ${babyData.diaries.size}")
            appendLine()

            if (chatHistory.isNotEmpty()) {
                appendLine("=== RECENT CONVERSATION ===")
                chatHistory.takeLast(4).forEach { message ->
                    appendLine(message)
                }
                appendLine()
            }

            appendLine("=== USER QUESTION ===")
            appendLine(userMessage)
            appendLine()
            appendLine("Provide a helpful, accurate response in Indonesian based on the baby's data.")
            appendLine("Always recommend consulting healthcare professionals for medical concerns.")
            appendLine("Use formal, clear, and supportive language without redundant introductions.")
        }
    }

    private fun parseAnalysisResponse(response: String): BabyAnalysisResult {

        // Clean the response to remove unwanted markers
        val cleanedResponse = response
            .replace(Regex("\\[.*?\\]"), "") // Remove square brackets and their content
            .replace(Regex("\\*\\*"), "") // Remove double asterisks
            .trim()

        val summary = extractSection(cleanedResponse, "SUMMARY:")
            .ifEmpty { "Tidak ada ringkasan tersedia." }
        val insights = extractListSection(cleanedResponse, "INSIGHTS:")
            .filter { it.isNotBlank() && !it.contains("Key insights about", ignoreCase = true) }
        val recommendations = extractListSection(cleanedResponse, "RECOMMENDATIONS:")
            .filter { it.isNotBlank() && !it.contains("Specific actionable recommendations", ignoreCase = true) }
        val concerns = extractListSection(cleanedResponse, "CONCERNS:")
            .filter { it.isNotBlank() && !it.contains("Any areas that need attention or monitoring", ignoreCase = true) }
            .ifEmpty { listOf("Tidak ada kekhawatiran saat ini.") }

        return BabyAnalysisResult(
            summary = summary,
            insights = insights,
            recommendations = recommendations,
            concerns = concerns
        )
    }

    private fun extractSection(text: String, marker: String): String {
        val startIndex = text.indexOf(marker, ignoreCase = true)
        if (startIndex == -1) return ""

        val startContent = startIndex + marker.length
        val endMarkers = listOf("INSIGHTS:", "RECOMMENDATIONS:", "CONCERNS:", "====")

        var endIndex = text.length
        for (endMarker in endMarkers) {
            val markerIndex = text.indexOf(endMarker, startContent, ignoreCase = true)
            if (markerIndex != -1 && markerIndex < endIndex) {
                endIndex = markerIndex
            }
        }

        return text.substring(startContent, endIndex).trim()
    }

    private fun extractListSection(text: String, marker: String): List<String> {
        val section = extractSection(text, marker)
        if (section.isEmpty()) return emptyList()

        return section.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("===") }
            .map { if (it.startsWith("-") || it.startsWith("•")) it.substring(1).trim() else it }
            .filter { it.isNotEmpty() }
    }

    private fun calculateAgeInMonths(birthDateMillis: Long): Int {
        val birthDate = Calendar.getInstance().apply { timeInMillis = birthDateMillis }
        val currentDate = Calendar.getInstance()
        var months = (currentDate.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)) * 12
        months += currentDate.get(Calendar.MONTH) - birthDate.get(Calendar.MONTH)
        if (currentDate.get(Calendar.DAY_OF_MONTH) < birthDate.get(Calendar.DAY_OF_MONTH)) {
            months--
        }
        return maxOf(0, months)
    }
}