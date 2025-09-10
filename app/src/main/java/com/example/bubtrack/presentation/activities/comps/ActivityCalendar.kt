package com.example.bubtrack.presentation.activities.comps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bubtrack.domain.activities.Activity
import com.example.bubtrack.ui.theme.AppGray
import com.example.bubtrack.ui.theme.AppPurple
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ActivityCalendar(
    modifier: Modifier = Modifier,
    activities: List<Activity>,
    onDateSelected: (LocalDate) -> Unit,
    calendarState: CalendarState
) {
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }

    // Convert activity dates to LocalDate
    val activityDates = activities.map {
        Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    HorizontalCalendar(
        state = calendarState,
        dayContent = { day ->
            val isSelected = selectedDate == day.date
            val hasActivity = activityDates.contains(day.date)

            Box(
                modifier = modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        when {
                            isSelected -> AppPurple
                            else -> Color.Transparent
                        }
                    )
                    .clickable {
                        selectedDate = day.date
                        onDateSelected(day.date)
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(4.dp)
                ) {
                    Text(
                        text = day.date.dayOfMonth.toString(),
                        color = if (isSelected) Color.White else Color.Black,
                        fontSize = 14.sp
                    )

                    // Titik di bawah tanggal jika ada aktivitas
                    if (hasActivity) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color.White else Color(0xFF8A56AC)
                                )
                        )
                    } else {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        },
        monthHeader = { month ->
            val daysOfWeek = month.weekDays.first().map { it.date.dayOfWeek }
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayOfWeek in daysOfWeek) {
                    Text(
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    )
                }
            }
        },
        modifier = modifier
            .shadow(
                elevation = 4.dp, // naikin dikit biar jelas
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color.Black.copy(alpha = 0.2f), // lebih halus
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(8.dp)
    )
}
