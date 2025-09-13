package com.example.bubtrack.presentation.ai.growthanalysis.comps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bubtrack.ui.theme.BubTrackTheme

@Composable
fun PeriodDropdown(
    modifier: Modifier = Modifier,
    selectedPeriod: String,
    periods: List<String> = listOf("Last 7 days", "Last 14 days", "Last 30 days"),
    onPeriodSelected: (String) -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDropdown = !showDropdown },
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedPeriod,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1F2937)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Dropdown",
                    tint = Color(0xFF6B7280)
                )
            }
        }

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false },
            modifier = Modifier.width(360.dp),
            offset = DpOffset(0.dp, 10.dp)
        ) {
            periods.forEach { period ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = period,
                            fontSize = 16.sp,
                            color = Color(0xFF1F2937)
                        )
                    },
                    onClick = {
                        onPeriodSelected(period)
                        showDropdown = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PeriodDropdownPreview() {
    BubTrackTheme {
        PeriodDropdown(
            modifier = Modifier.padding(16.dp),
            selectedPeriod = "Last 7 days",
            onPeriodSelected = {}
        )
    }
}