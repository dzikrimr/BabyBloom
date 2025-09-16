package com.example.bubtrack.presentation.home

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bubtrack.R
import com.example.bubtrack.presentation.diary.comps.StatsCardItem
import com.example.bubtrack.presentation.home.comps.GrowthStatsSection
import com.example.bubtrack.presentation.home.comps.HomeHeader
import com.example.bubtrack.presentation.home.comps.SmartBabyCareSection
import com.example.bubtrack.presentation.home.comps.UpcomingActivitiesSection
import com.example.bubtrack.presentation.navigation.ActivitiesRoute
import com.example.bubtrack.presentation.navigation.AppRoute
import com.example.bubtrack.presentation.navigation.CryAnalyzerRoute
import com.example.bubtrack.presentation.navigation.DiaryRoute
import com.example.bubtrack.presentation.navigation.NotificationRoute
import com.example.bubtrack.presentation.navigation.SleepMonitorRoute
import com.example.bubtrack.ui.theme.AppBackground
import com.example.bubtrack.ui.theme.AppBlue
import com.example.bubtrack.ui.theme.AppLightBlue
import com.example.bubtrack.ui.theme.AppLightPurple
import com.example.bubtrack.ui.theme.AppPink
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme
import java.time.LocalDate

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    navigate : (AppRoute) -> Unit
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
                onCryAnalyzerClick = { navigate(CryAnalyzerRoute)},
                onSleepMonitorClick = { navigate(SleepMonitorRoute)}
            )

            Spacer(modifier = Modifier.height(22.dp))

            GrowthStatsSection(
                statsList = statsList,
                onSeeAllClick = { navigate(DiaryRoute) }
            )

            Spacer(modifier = Modifier.height(22.dp))

            UpcomingActivitiesSection(
                activities = uiState.upcomingActivities,
                today = today,
                onSeeAllClick = { navigate(ActivitiesRoute) }
            )

            Spacer(modifier = Modifier.height(22.dp))
        }
    }
}

