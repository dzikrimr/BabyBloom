package com.example.bubtrack.presentation.activities.comps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bubtrack.R
import com.example.bubtrack.domain.activities.Activity
import com.example.bubtrack.domain.activities.ActivityType
import com.example.bubtrack.ui.theme.AppBackground
import com.example.bubtrack.ui.theme.AppBlue
import com.example.bubtrack.ui.theme.AppPink
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ActivityCard(
    modifier: Modifier = Modifier,
    activity: Activity
) {
    val bgColor = when (activity.type) {
        ActivityType.CHECKUP.value -> AppBlue
        ActivityType.FEEDING.value -> AppPink
        ActivityType.PLAYTIME.value -> AppPurple
        else -> Color(0xFFF87171)
    }

    // Map activity type to icon
    val iconRes = when (activity.type) {
        "vaccine" -> R.drawable.ic_vaccine
        "checkup" -> R.drawable.ic_checkup
        "feeding" -> R.drawable.ic_feeding
        "playtime" -> R.drawable.ic_playtime
        else -> R.drawable.ic_other
    }

    val formatter = SimpleDateFormat("dd MMMM", Locale("id", "ID"))
    val currentDate = formatter.format(activity.date)
    val formattedTime = String.format("%02d:%02d", activity.hour, activity.minute)

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color.White)
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = "${activity.type} icon",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = activity.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = activity.type,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = "$currentDate • $formattedTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                }
            }
            Text(
                text = activity.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    BubTrackTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBackground)
                .padding(14.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ActivityCard(
                activity = Activity(
                    id = 2,
                    userId = "user1",
                    title = "Check-up rutin",
                    description = "Pediatric visit",
                    date = System.currentTimeMillis() + 86400000,
                    hour = 12,
                    minute = 30,
                    type = ActivityType.CHECKUP.value
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            ActivityCard(
                activity = Activity(
                    id = 3,
                    userId = "user1",
                    title = "Kegiatan Lain",
                    description = "Kunjungan ke dokter mata",
                    date = System.currentTimeMillis() + 2 * 86400000,
                    hour = 14,
                    minute = 0,
                    type = "other"
                )
            )
        }
    }
}