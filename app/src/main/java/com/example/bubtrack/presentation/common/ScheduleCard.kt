package com.example.bubtrack.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bubtrack.R
import com.example.bubtrack.domain.activities.Activity
import com.example.bubtrack.ui.theme.BubTrackTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import java.util.Locale

@Composable
fun ScheduleCard(
    modifier: Modifier = Modifier,
    activity: Activity,
    today: LocalDate
) {
    // Calculate days until activity
    val activityDate = Instant.ofEpochMilli(activity.date)
        .atZone(ZoneId.systemDefault()).toLocalDate()
    val daysUntil = ChronoUnit.DAYS.between(today, activityDate).toInt()

    // Determine colors based on proximity
    val (cardColor, circleColor) = when {
        daysUntil <= 1 -> Color(0xFFFBCFE8) to Color(0xFFF87171).copy(alpha = 0.2f)
        daysUntil <= 4 -> Color(0xFFFBF4CF) to Color(0xFFF8BE71).copy(alpha = 0.2f)
        else -> Color(0xFFBFDBFE) to Color(0xFF93C5FD).copy(alpha = 0.2f)
    }

    // Map activity type to icon
    val iconRes = when (activity.type) {
        "Vaccine" -> R.drawable.ic_vaccine
        "Check-Up" -> R.drawable.ic_checkup
        "Feeding" -> R.drawable.ic_feeding
        "Playtime" -> R.drawable.ic_playtime
        else -> R.drawable.ic_other
    }

    // Format date and time
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("id"))
    val dateString = activityDate.format(dateFormatter)
    val timeString = String.format("%02d:%02d", activity.hour, activity.minute)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(cardColor)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(color = circleColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = "${activity.type} icon",
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
        Column(
            modifier = modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = activity.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "$dateString, $timeString",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4B5563)
            )
            Text(
                text = activity.description.takeIf { it.isNotBlank() } ?: "Tidak ada deskripsi",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF4B5563)
            )
        }
    }
}

@Preview
@Composable
private fun ScheduleCardPreview() {
    BubTrackTheme {
        ScheduleCard(
            activity = Activity(
                userId = "user1",
                title = "Vaksin Polio",
                description = "Puskesmas Glonggong",
                date = System.currentTimeMillis(),
                hour = 10,
                minute = 30,
                type = "Other"
            ),
            today = LocalDate.now()
        )
    }
}