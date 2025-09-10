package com.example.bubtrack.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun OverallStatusCard(
    status: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                status.contains("PERHATIAN") -> Color.Red.copy(alpha = 0.1f)
                status.contains("tidur") -> Color.Green.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}