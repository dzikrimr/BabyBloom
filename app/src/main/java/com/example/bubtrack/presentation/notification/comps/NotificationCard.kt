package com.example.bubtrack.presentation.notification.comps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bubtrack.R
import com.example.bubtrack.utill.Utility

data class NotificationItem(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = 0,
    val type: String = "", // ACTIVITY, AI, DIARY, ACTIVITY_REMINDER, DIARY_REMINDER
    val isRead: Boolean = false,
    val activityId: String? = null // for activity reminders
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCard(
    notification: NotificationItem,
    modifier: Modifier = Modifier,
    onMarkAsRead: (String) -> Unit = {},
    onDelete: (String) -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (notification.isRead) 0.6f else 1f),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.Gray.copy(alpha = 0.1f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isRead) 0.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with background circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (notification.type) {
                            "ACTIVITY_REMINDER" -> Color(0xFF4CAF50)
                            "DIARY_REMINDER" -> Color(0xFF2196F3)
                            "ACTIVITY" -> Color(0xFFA78BFA)
                            "AI" -> Color(0xFFFF9800)
                            "DIARY" -> Color(0xFF2196F3)
                            else -> Color(0xFF9E9E9E)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = when (notification.type) {
                            "ACTIVITY_REMINDER" -> R.drawable.ic_notification
                            "DIARY_REMINDER" -> R.drawable.ic_diary
                            "ACTIVITY" -> R.drawable.ic_feeding
                            "AI" -> R.drawable.ic_ai
                            "DIARY" -> R.drawable.ic_diary
                            else -> R.drawable.ic_notification
                        }
                    ),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.title,
                    fontSize = 14.sp,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Medium,
                    color = if (notification.isRead) Color.Gray else Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (notification.message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = notification.message,
                        fontSize = 12.sp,
                        color = if (notification.isRead) Color.Gray else Color.Black.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = Utility.formatNotificationTimestamp(notification.timestamp),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Action buttons
            if (!notification.isRead) {
                IconButton(
                    onClick = { onMarkAsRead(notification.id) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_markread),
                        contentDescription = "Mark as read",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            IconButton(
                onClick = { onDelete(notification.id) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = "Delete",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}