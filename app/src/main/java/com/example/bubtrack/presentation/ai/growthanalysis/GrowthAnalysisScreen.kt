package com.example.bubtrack.presentation.ai.growthanalysis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bubtrack.R
import com.example.bubtrack.presentation.ai.growthanalysis.comps.AiChatSection
import com.example.bubtrack.presentation.ai.growthanalysis.comps.GrowthSummarySection
import com.example.bubtrack.presentation.ai.growthanalysis.comps.PeriodDropdown
import com.example.bubtrack.ui.theme.BubTrackTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthAnalysisScreen(
    modifier: Modifier = Modifier,
    viewModel: GrowthAnalysisViewModel = hiltViewModel(),
    userId: String,
    onNavigateBack: () -> Unit
) {
    // Initialize ViewModel with userId
    LaunchedEffect(userId) {
        viewModel.initialize(userId)
    }

    // Collect UI state
    val uiState by viewModel.uiState.collectAsState()

    // Handle errors
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error in a Snackbar or similar
            // For simplicity, we'll clear it after display
            viewModel.clearError()
        }
    }

    val periods = listOf("Last 7 days", "Last 14 days", "Last 30 days")

    if (!uiState.isReady) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Generating personalized analysis...",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    } else {

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            // Header
            item {
                GrowthAnalysisHeader(onNavigateBack = onNavigateBack)
            }

            // Content
            item {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Description text
                    Text(
                        text = "These insights are based on the daily development and growth records you provide.",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Baby illustration card
                    BabyIllustrationCard()

                    // Dropdown for time period
                    PeriodDropdown(
                        selectedPeriod = uiState.selectedPeriod,
                        periods = periods,
                        onPeriodSelected = { period ->
                            viewModel.updateSelectedPeriod(period)
                        }
                    )

                    // Loading indicator
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        // Growth Summary Section
                        GrowthSummarySection(
                            summaryText = uiState.analysisResult?.summary
                                ?: "No analysis available yet.",
                            iconRes = com.example.bubtrack.R.drawable.ic_growth,
                            backgroundColor = Color(0xFFE0F7FF),
                            iconBackgroundColor = Color(0xFF06B6D4)
                        )

                        // Additional analysis details (insights, recommendations, concerns)
                        uiState.analysisResult?.let { result ->
                            AnalysisDetailsSection(
                                insights = result.insights,
                                recommendations = result.recommendations,
                                concerns = result.concerns
                            )
                        }

                        // Gemini AI Assistant Section
                        AiChatSection(
                            chatMessages = uiState.chatMessages,
                            isLoading = uiState.isChatLoading,
                            onSendMessage = { message ->
                                viewModel.sendChatMessage(message)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BabyIllustrationCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8EFFC)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_babyillustration),
                contentDescription = "Baby Illustration",
                modifier = Modifier.size(300.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun GrowthAnalysisHeader(
    onNavigateBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF6B7280),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onNavigateBack() }
            )

            Text(
                text = "Growth Analysis",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937)
            )

            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun AnalysisDetailsSection(
    insights: List<String>,
    recommendations: List<String>,
    concerns: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (insights.isNotEmpty()) {
                AnalysisSection(
                    title = "Insights",
                    items = insights,
                    iconRes = com.example.bubtrack.R.drawable.ic_insights,
                    iconBackgroundColor = Color(0xFF10B981)
                )
            }
            if (recommendations.isNotEmpty()) {
                AnalysisSection(
                    title = "Recommendations",
                    items = recommendations,
                    iconRes = com.example.bubtrack.R.drawable.ic_recommendations,
                    iconBackgroundColor = Color(0xFF3B82F6)
                )
            }
            if (concerns.isNotEmpty()) {
                AnalysisSection(
                    title = "Concerns",
                    items = concerns,
                    iconRes = com.example.bubtrack.R.drawable.ic_concerns,
                    iconBackgroundColor = Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
private fun AnalysisSection(
    title: String,
    items: List<String>,
    iconRes: Int,
    iconBackgroundColor: Color
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(iconBackgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        items.forEach { item ->
            Text(
                text = "• $item",
                fontSize = 14.sp,
                color = Color(0xFF374151),
                lineHeight = 20.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GrowthAnalysisScreenPreview() {
    BubTrackTheme {
        GrowthAnalysisScreen(
            userId = "testUser",
            onNavigateBack = {}
        )
    }
}