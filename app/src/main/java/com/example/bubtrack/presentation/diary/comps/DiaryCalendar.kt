package com.example.bubtrack.presentation.diary.comps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bubtrack.ui.theme.AppPurple
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DiaryCalendar(
    modifier: Modifier = Modifier,
    diaryDates: List<LocalDate>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit,
    calendarState: CalendarState
) {
    var localSelectedDate by remember { mutableStateOf(selectedDate) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(8.dp)
    ) {
        // Month and Year Header
        val currentMonth = calendarState.firstVisibleMonth.yearMonth
        Text(
            text = currentMonth.format(
                DateTimeFormatter.ofPattern("MMMM yyyy", Locale("id"))
            ).replaceFirstChar { it.uppercase() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        HorizontalCalendar(
            state = calendarState,
            dayContent = { day ->
                val isSelected = localSelectedDate == day.date

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                isSelected -> AppPurple
                                else -> Color.Transparent
                            }
                        )
                        .clickable {
                            localSelectedDate = if (localSelectedDate == day.date) null else day.date
                            onDateSelected(localSelectedDate)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.date.dayOfMonth.toString(),
                        color = if (isSelected) Color.White else Color.Black,
                        fontSize = 14.sp
                    )
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
                            color = Color.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        )
    }
}