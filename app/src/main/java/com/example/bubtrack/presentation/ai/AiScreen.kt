package com.example.bubtrack.presentation.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bubtrack.R
import com.example.bubtrack.presentation.ai.comps.BabyCareCard
import com.example.bubtrack.presentation.navigation.CryAnalyzerRoute
import com.example.bubtrack.presentation.navigation.SleepMonitorRoute
import com.example.bubtrack.ui.theme.AppBackground
import com.example.bubtrack.ui.theme.BubTrackTheme

@Composable
fun AiScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = modifier.width(25.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "back button",
                    modifier = modifier.fillMaxSize()
                )
            }
            Text(
                "Bloom AI",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Spacer(modifier = modifier.width(25.dp))
        }
        Spacer(modifier.height(24.dp))
        Text(
            text = "Smart Baby Care",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            "AI-powered features to help you understand and monitor your baby better",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color(0xFF4B5563),
            modifier = modifier.padding(vertical = 4.dp, horizontal = 50.dp)
        )
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            BabyCareCard(
                title = "Cry Detection & Analysis",
                description = "Advanced AI analyzes your baby's cries to identify their needs instantly",
                img = R.drawable.img_cry,
                btnText = "Start Cry Analysis"
            ) {
                navController.navigate(CryAnalyzerRoute)
            }
            Spacer(modifier.height(22.dp))
            BabyCareCard(
                title = "Real-time Sleep Monitor",
                description = "Monitor your baby remotely using another phone's camera with instant notifications",
                img = R.drawable.img_sleep,
                btnText = "Start Monitoring"
            ) {
                navController.navigate(SleepMonitorRoute)
            }
        }

    }
}

@Preview
@Composable
private fun Preview() {
    BubTrackTheme {
        AiScreen(
            navController = rememberNavController()
        )
    }
}