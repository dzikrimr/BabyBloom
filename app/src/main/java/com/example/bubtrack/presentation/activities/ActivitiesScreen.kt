package com.example.bubtrack.presentation.activities

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bubtrack.domain.activities.Activity
import com.example.bubtrack.domain.activities.ActivityType
import com.example.bubtrack.domain.activities.dummyActivities
import com.example.bubtrack.presentation.activities.comps.ActivityCalendar
import com.example.bubtrack.presentation.activities.comps.ActivityCard
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

@Composable
fun ActivitiesScreen(
    modifier: Modifier = Modifier,
    viewModel: ActivitiesViewModel = hiltViewModel()
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


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.White)
                .statusBarsPadding()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {},
                modifier = modifier.width(25.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "back button",
                    modifier = modifier.fillMaxSize()
                )
            }
            Text(
                "Activities",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Spacer(modifier = modifier.width(25.dp))
        }
        Spacer(modifier.height(18.dp))

        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                .padding(14.dp) // padding di luar container
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
                .padding(14.dp),
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
                    Spacer(modifier = modifier.height(18.dp))
                    val data = uiState.activitiesForSelectedDate
                    LazyColumn(
                        modifier = modifier.fillMaxSize()
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

@Preview
@Composable
private fun Preview() {
    BubTrackTheme {
        ActivitiesScreen()
    }
}