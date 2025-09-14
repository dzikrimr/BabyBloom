package com.example.bubtrack.presentation.home

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bubtrack.R
import com.example.bubtrack.domain.activities.Activity
import com.example.bubtrack.domain.home.HomeRepo
import com.example.bubtrack.presentation.common.ScheduleCard
import com.example.bubtrack.presentation.diary.comps.StatsCard
import com.example.bubtrack.presentation.diary.comps.StatsCardItem
import com.example.bubtrack.presentation.home.comps.GrowthStatsSection
import com.example.bubtrack.presentation.home.comps.HomeHeader
import com.example.bubtrack.presentation.home.comps.SmartBabyCareSection
import com.example.bubtrack.presentation.home.comps.UpcomingActivitiesSection
import com.example.bubtrack.presentation.navigation.ActivitiesRoute
import com.example.bubtrack.presentation.navigation.CryAnalyzerRoute
import com.example.bubtrack.presentation.navigation.NotificationRoute
import com.example.bubtrack.presentation.navigation.SleepMonitorRoute
import com.example.bubtrack.ui.theme.AppBackground
import com.example.bubtrack.ui.theme.AppBlue
import com.example.bubtrack.ui.theme.AppLightBlue
import com.example.bubtrack.ui.theme.AppLightPurple
import com.example.bubtrack.ui.theme.AppPink
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val today = LocalDate.now()

    // Handle error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    // List of images for the carousel
    val images = listOf(
        R.drawable.img_baby1,
        R.drawable.img_baby2,
        R.drawable.img_baby3
    )

    val statsList = listOf(
        StatsCardItem(
            title = "Berat",
            value = uiState.latestGrowthStats.weight,
            icon = R.drawable.ic_weightscale,
            unit = "kg",
            bgColor = AppPurple
        ),
        StatsCardItem(
            title = "Tinggi",
            value = uiState.latestGrowthStats.height,
            icon = R.drawable.ic_height,
            unit = "cm",
            bgColor = AppBlue
        ),
        StatsCardItem(
            title = "L. Kepala",
            value = uiState.latestGrowthStats.headCircum,
            icon = R.drawable.ic_head,
            unit = "cm",
            bgColor = AppPink
        ),
        StatsCardItem(
            title = "L. Lengan",
            value = uiState.latestGrowthStats.armCircum,
            icon = R.drawable.ic_lengan,
            unit = "cm",
            bgColor = AppLightPurple
        ),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(scrollState)
    ) {

        HomeHeader(
            babyName = uiState.babyProfile?.babyName ?: "Sarah",
            babyAge = uiState.babyAge,
            currentImageIndex = uiState.currentImageIndex,
            images = images,
            onNotificationClick = { navController.navigate(NotificationRoute) }
        )

        // Content Section
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            SmartBabyCareSection(
                onCryAnalyzerClick = { navController.navigate(CryAnalyzerRoute) },
                onSleepMonitorClick = { navController.navigate(SleepMonitorRoute) }
            )

            Spacer(modifier = Modifier.height(22.dp))

            GrowthStatsSection(
                statsList = statsList,
                onSeeAllClick = { navController.navigate("diary/Growth Chart") }
            )

            Spacer(modifier = Modifier.height(22.dp))

            UpcomingActivitiesSection(
                activities = uiState.upcomingActivities,
                today = today,
                onSeeAllClick = { navController.navigate(ActivitiesRoute) }
            )

            Spacer(modifier = Modifier.height(22.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    BubTrackTheme {
        HomeScreen(navController = rememberNavController())
    }
}