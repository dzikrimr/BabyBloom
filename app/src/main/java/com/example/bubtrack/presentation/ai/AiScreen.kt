package com.example.bubtrack.presentation.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bubtrack.R
import com.example.bubtrack.presentation.ai.comps.BabyCareCard
import com.example.bubtrack.presentation.navigation.CryAnalyzerRoute
import com.example.bubtrack.presentation.navigation.GrowthAnalysisRoute
import com.example.bubtrack.presentation.navigation.SleepMonitorRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiScreen(
    navController: NavController,
    onStartCryAnalysis: () -> Unit = { navController.navigate(CryAnalyzerRoute) },
    onStartMonitoring: () -> Unit = { navController.navigate(SleepMonitorRoute) },
    onStartAnalysis: () -> Unit = { navController.navigate(GrowthAnalysisRoute) }
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.width(25.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.fillMaxSize(),
                        tint = Color.Black
                    )
                }
                Text(
                    text = "Bloom AI",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(25.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Section with Gradient Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFBFDBFE),
                                Color(0x1AFAF5FF)
                            )
                        )
                    )
                    .padding(vertical = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Smart Baby Care",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "AI-powered features to help you understand and monitor your baby better",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // Cry Detection Card
                BabyCareCard(
                    title = "Cry Detection & Analysis",
                    description = "Advanced AI analyzes your baby's cries to identify their needs instantly",
                    img = R.drawable.img_cry,
                    btnText = "Start Cry Analysis",
                    onClick = onStartCryAnalysis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Sleep Monitor Card
                BabyCareCard(
                    title = "Real-time Sleep Monitor",
                    description = "Monitor your baby remotely using another phone's camera with instant notifications",
                    img = R.drawable.img_sleep,
                    btnText = "Start Monitoring",
                    onClick = onStartMonitoring,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Growth & Development Analysis Card
                BabyCareCard(
                    title = "Growth & Development Analysis",
                    description = "Personalized analysis of your baby's progress, milestones, and recommendations.",
                    img = R.drawable.img_analysis,
                    btnText = "Start Analysis",
                    onClick = onStartAnalysis,
                    badgeDrawableId = R.drawable.gemini_badge,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}