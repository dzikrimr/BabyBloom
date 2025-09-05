package com.example.bubtrack.presentation.diary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.bubtrack.R
import com.example.bubtrack.ui.theme.AppBackground
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme

@Composable
fun DiaryScreen(modifier: Modifier = Modifier) {

    var selectedTab by remember {
        mutableStateOf("Development")
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(14.dp),
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {},
                    modifier = modifier.width(25.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "back button",
                        modifier = modifier.fillMaxSize()
                    )
                }
                Text(
                    "Baby Diary",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Spacer(modifier = modifier.width(25.dp))
            }

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(AppPurple)

                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_calendar2),
                            contentDescription = "calendar",
                            modifier = modifier.align(Alignment.Center),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = modifier.width(8.dp))
                    Text(
                        "15 Maret 2024",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                }
                Text(
                    "Ganti",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppPurple
                )
            }
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        selectedTab = "Development"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == "Development") AppPurple else Color(
                            0xFFF3F4F6
                        ),
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(width = 0.dp, color = Color.Transparent)
                ) {
                    Text(
                        "Development",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (selectedTab == "Development") Color.White else Color(0xFF6B7280)
                    )
                }
                Spacer(modifier.width(12.dp))
                OutlinedButton(
                    onClick = {
                        selectedTab = "Growth Chart"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == "Growth Chart") AppPurple else Color(
                            0xFFF3F4F6
                        ),
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(width = 0.dp, color = Color.Transparent)
                ) {
                    Text(
                        "Growth Chart",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (selectedTab == "Growth Chart") Color.White else Color(0xFF6B7280)
                    )
                }
            }
        }
        when (selectedTab) {
            "Development" -> DevelopmentScreen()
            "Growth Chart" -> GrowthChartScreen()
        }

    }
}

@Composable
@Preview(showBackground = true)
fun DiaryScreenPreview() {
    BubTrackTheme {
        DiaryScreen()
    }

}