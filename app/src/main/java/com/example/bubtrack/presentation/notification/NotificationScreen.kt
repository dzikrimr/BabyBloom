package com.example.bubtrack.presentation.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bubtrack.presentation.notification.comps.NotificationCard
import com.example.bubtrack.presentation.notification.comps.NotificationItem
import com.example.bubtrack.presentation.notification.comps.NotificationType

@Composable
fun NotificationScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val notifications = listOf(
        NotificationItem(
            id = "1",
            title = "Your baby may roll over, let's check it out.",
            timestamp = "17 Agustus • 4:00 PM",
            type = NotificationType.ACTIVITY
        ),
        NotificationItem(
            id = "2",
            title = "Your baby was sleeping",
            timestamp = "17 Agustus • 4:00 PM",
            type = NotificationType.ACTIVITY
        ),
        NotificationItem(
            id = "3",
            title = "Your baby was wakeup",
            timestamp = "17 Agustus • 4:00 PM",
            type = NotificationType.ACTIVITY
        ),
        NotificationItem(
            id = "4",
            title = "Besok waktunya Vaccination",
            timestamp = "17 Agustus • 4:00 PM",
            type = NotificationType.AI
        ),
        NotificationItem(
            id = "5",
            title = "Ayo catat diary dan perkembangan si bayi",
            timestamp = "17 Agustus • 4:00 PM",
            type = NotificationType.DIARY
        ),
        NotificationItem(
            id = "6",
            title = "Your baby was crying",
            timestamp = "17 Agustus • 4:00 PM",
            type = NotificationType.ACTIVITY
        ),
        NotificationItem(
            id = "7",
            title = "Your baby looks like moving actively",
            timestamp = "17 Agustus • 4:00 PM",
            type = NotificationType.ACTIVITY
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header
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
                    onClick = onBackClick,
                    modifier = Modifier.width(25.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Text(
                    text = "Notification",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(25.dp))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifications) { notification ->
                NotificationCard(
                    notification = notification
                )
            }
        }
    }
}