package com.example.bubtrack.presentation.activities

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bubtrack.domain.activities.Activity
import com.example.bubtrack.presentation.activities.comps.ActivityCalendar
import com.example.bubtrack.presentation.activities.comps.ActivityCard
import com.example.bubtrack.presentation.activities.comps.AddSchedulePopUp
import com.example.bubtrack.ui.theme.AppBackground
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme
import com.example.bubtrack.utill.Resource
import com.kizitonwose.calendar.compose.rememberCalendarState
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ActivitiesScreen(
    modifier: Modifier = Modifier,
    viewModel: ActivitiesViewModel = hiltViewModel(),
    navController: androidx.navigation.NavController? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    val currentMonth = YearMonth.now()
    val calendarState = rememberCalendarState(
        startMonth = currentMonth.minusMonths(12),
        endMonth = currentMonth.plusMonths(12),
        firstDayOfWeek = DayOfWeek.SUNDAY,
        firstVisibleMonth = currentMonth
    )
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = !showDialog },
                shape = CircleShape,
                containerColor = AppPurple
            ) {
                Icon(
                    painter = painterResource(id = com.example.bubtrack.R.drawable.ic_add),
                    contentDescription = "add activity",
                    tint = Color.White,
                    modifier = modifier.size(20.dp)
                )
            }
        }
    ) { _ ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(AppBackground)
                .statusBarsPadding()
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
                    onClick = { navController?.popBackStack() ?: {} },
                    modifier = modifier.width(25.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "back button",
                        modifier = modifier.fillMaxSize()
                    )
                }
                Text(
                    "Schedule",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Spacer(modifier = modifier.width(25.dp))
            }
            Spacer(modifier = modifier.height(8.dp))

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    Icons.AutoMirrored.Default.KeyboardArrowLeft,
                    contentDescription = "back button",
                    modifier = modifier
                        .size(36.dp)
                        .clickable {
                            coroutineScope.launch {
                                val prevMonth = calendarState.firstVisibleMonth.yearMonth.minusMonths(1)
                                calendarState.animateScrollToMonth(prevMonth)
                            }
                        },
                    tint = AppPurple
                )
                Text(
                    calendarState.firstVisibleMonth.yearMonth
                        .month
                        .getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                            " " + calendarState.firstVisibleMonth.yearMonth.year,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Icon(
                    Icons.AutoMirrored.Default.KeyboardArrowRight,
                    contentDescription = "back button",
                    modifier = modifier
                        .size(36.dp)
                        .clickable {
                            coroutineScope.launch {
                                val prevMonth = calendarState.firstVisibleMonth.yearMonth.plusMonths(1)
                                calendarState.animateScrollToMonth(prevMonth)
                            }
                        },
                    tint = AppPurple
                )
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                ActivityCalendar(
                    activities = uiState.allActivities,
                    onDateSelected = { date ->
                        viewModel.onDateSelected(date)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    calendarState = calendarState
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    "Activities on This Day",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                when {
                    uiState.isLoading -> {
                        Text("Loading...")
                    }
                    uiState.isError != null -> {
                        Text("Error: ${uiState.isError}")
                    }
                    uiState.activitiesForSelectedDate.isEmpty() -> {
                        Text("No activities on this date")
                    }
                    else -> {
                        Spacer(modifier = modifier.height(12.dp))
                        val data = uiState.activitiesForSelectedDate
                        LazyColumn(
                            modifier = modifier
                                .fillMaxSize()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(data.size) {
                                ActivityCard(activity = data[it])
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddSchedulePopUp(
            onDismiss = { showDialog = false },
            onSave = { activityName, activityType, dateMillis, selectedTime, notes ->
                val hour = selectedTime?.hour ?: 0
                val minute = selectedTime?.minute ?: 0
                val activity = Activity(
                    userId = "user1",
                    title = activityName,
                    description = notes,
                    date = dateMillis,
                    hour = hour,
                    minute = minute,
                    type = activityType
                )
                viewModel.addActivity(activity)
            }
        )
    }
}

@Preview
@Composable
private fun Preview() {
    BubTrackTheme {
        ActivitiesScreen()
    }
}