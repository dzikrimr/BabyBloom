package com.example.bubtrack.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bubtrack.data.models.SleepStatus

@Composable
fun StatusCard(
    title: String,
    status: SleepStatus,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            StatusItem("👁️ Mata", status.eyeStatus)
            StatusItem("🏃 Pergerakan", status.movementStatus)
            StatusItem("🔄 Rollover", status.rolloverStatus)
        }
    }
}