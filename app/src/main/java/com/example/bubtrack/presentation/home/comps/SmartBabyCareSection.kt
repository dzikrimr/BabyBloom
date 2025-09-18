package com.example.bubtrack.presentation.home.comps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bubtrack.R
import com.example.bubtrack.ui.theme.AppLightBlue
import com.example.bubtrack.ui.theme.AppLightPurple
import com.example.bubtrack.ui.theme.AppPurple

@Composable
fun SmartBabyCareSection(
    onCryAnalyzerClick: () -> Unit,
    onSleepMonitorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        "Perawatan Bayi Cerdas",
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SmartCareCard(
            title = "Analisis Tangisan",
            description = "Pahami tangisan si kecil",
            icon = R.drawable.ic_mic,
            backgroundColor = AppLightPurple,
            iconBackgroundColor = AppPurple.copy(alpha = 0.2f),
            onClick = onCryAnalyzerClick,
            modifier = Modifier.fillMaxWidth(0.5f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        SmartCareCard(
            title = "Monitor tidur",
            description = "Pantau tidur si kecil",
            icon = R.drawable.ic_moon,
            backgroundColor = AppLightBlue,
            iconBackgroundColor = Color(0xFF93C5FD).copy(alpha = 0.2f),
            onClick = onSleepMonitorClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}