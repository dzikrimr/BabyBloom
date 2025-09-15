package com.example.bubtrack.presentation.home.comps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.bubtrack.domain.activities.Activity
import com.example.bubtrack.presentation.common.ScheduleCard
import com.example.bubtrack.ui.theme.AppPurple
import java.time.LocalDate

@Composable
fun UpcomingActivitiesSection(
    activities: List<Activity>,
    today: LocalDate,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "Jadwal Terdekat",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Text(
            "Lihat Semua",
            style = MaterialTheme.typography.bodyMedium.copy(color = AppPurple),
            modifier = Modifier.clickable { onSeeAllClick() }
        )
    }
    Spacer(modifier = Modifier.height(12.dp))

    if (activities.isEmpty()) {
        Column (
            modifier = modifier.fillMaxSize().padding(16.dp),
           horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Tidak ada jadwal dalam 7 hari ke depan",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(end = 4.dp)
            )
            Spacer(modifier.height(12.dp))
            Text(
                "Tambahkan jadwal baru",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppPurple,
                textDecoration = TextDecoration.Underline,
                modifier = modifier.clickable { onSeeAllClick() }
            )
        }

    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            activities.forEach { activity ->
                ScheduleCard(
                    activity = activity,
                    today = today
                )
            }
        }
    }
}