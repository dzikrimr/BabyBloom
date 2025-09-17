package com.example.bubtrack.presentation.diary

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bubtrack.R
import com.example.bubtrack.presentation.diary.comps.DiaryCalendar
import com.example.bubtrack.ui.theme.AppBackground
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme
import com.kizitonwose.calendar.compose.rememberCalendarState
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DiaryScreen(
    modifier: Modifier = Modifier,
    initialTab: String = "Catatan",
    viewModel: DiaryViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    var showCalendarDialog by remember { mutableStateOf(false) }
    val selectedDate by viewModel.selectedDate.collectAsState()
    val diaryList by viewModel.diaryState.collectAsState()
    val growthList by viewModel.uiState.collectAsState()
    val calendarState = rememberCalendarState(
        startMonth = YearMonth.now().minusYears(2),
        endMonth = YearMonth.now().plusYears(2),
        firstVisibleMonth = YearMonth.now()
    )

    val formattedDate = selectedDate?.let {
        it.format(DateTimeFormatter.ofPattern("dd MMMM, yyyy", Locale("id")))
    } ?: "Semua Tanggal"

    val diaryDates = diaryList.map { diary ->
        Instant.ofEpochMilli(diary.date).atZone(ZoneId.systemDefault()).toLocalDate()
    } + growthList.isSuccess.map { growth ->
        Instant.ofEpochMilli(growth.date).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        item {
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
                        onClick = {},
                        modifier = Modifier.width(25.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "back button",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Text(
                        "Baby Diary",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Spacer(modifier = Modifier.width(25.dp))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(AppPurple)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_calendar2),
                                contentDescription = "calendar",
                                modifier = Modifier.align(Alignment.Center),
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            formattedDate,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                    Text(
                        "Ganti",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppPurple,
                        modifier = Modifier.clickable { showCalendarDialog = true }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { selectedTab = "Catatan" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == "Catatan") AppPurple else Color(0xFFF3F4F6),
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(width = 0.dp, color = Color.Transparent)
                    ) {
                        Text(
                            "Catatan",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (selectedTab == "Catatan") Color.White else Color(0xFF6B7280)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = { selectedTab = "Perkembangan" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == "Perkembangan") AppPurple else Color(0xFFF3F4F6),
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(width = 0.dp, color = Color.Transparent)
                    ) {
                        Text(
                            "Perkembangan",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (selectedTab == "Perkembangan") Color.White else Color(0xFF6B7280)
                        )
                    }
                }
            }
        }
        item {
            when (selectedTab) {
                "Catatan" -> DevelopmentScreen(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    viewModel = viewModel
                )
                "Perkembangan" -> GrowthChartScreen(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    viewModel = viewModel
                )
            }
        }
    }

    // Calendar Dialog
    if (showCalendarDialog) {
        Dialog(
            onDismissRequest = { showCalendarDialog = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                var tempSelectedDate by remember { mutableStateOf(selectedDate) }
                DiaryCalendar(
                    diaryDates = diaryDates,
                    selectedDate = tempSelectedDate,
                    onDateSelected = { date ->
                        tempSelectedDate = date
                    },
                    calendarState = calendarState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { showCalendarDialog = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF3F4F6),
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(width = 0.dp, color = Color.Transparent)
                    ) {
                        Text(
                            "Batal",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.setSelectedDate(tempSelectedDate)
                            showCalendarDialog = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppPurple,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(width = 0.dp, color = Color.Transparent)
                    ) {
                        Text(
                            "OK",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiaryScreenPreview() {
    BubTrackTheme {
        DiaryScreen()
    }
}