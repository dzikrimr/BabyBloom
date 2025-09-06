package com.example.bubtrack.presentation.diary.comps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.bubtrack.domain.growth.BabyGrowthModel
import com.example.bubtrack.ui.theme.AppGray
import com.example.bubtrack.ui.theme.BubTrackTheme
import com.example.bubtrack.utill.Utility

@Composable
fun GrowthCard(
    modifier: Modifier = Modifier,
    babyGrowth: BabyGrowthModel,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(12.dp)
    ){
        Text(
            Utility.formatPrettyDate(babyGrowth.date),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier.height(12.dp))
        Row(
            modifier = modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Berat: ${babyGrowth.weight?.toInt()} kg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppGray
                )
                Text(
                    "Tinggi: ${babyGrowth.height?.toInt()} cm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppGray
                )
            }
            Column {
                Text(
                    "L. Kepala: ${babyGrowth.weight?.toInt()} cm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppGray
                )
                Text(
                    "L. Lengan: ${babyGrowth.height?.toInt()} cm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppGray
                )
            }
        }
    }
}


@Preview
@Composable
private fun Test() {
    BubTrackTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GrowthCard(
                babyGrowth = BabyGrowthModel(
                    id = "1",
                    date = System.currentTimeMillis(),
                    weight = 3.2,
                    height = 52.0,
                    headCircumference = 35.0,
                    armLength = 18.0,
                    ageInMonths = 0
                )
            )
        }
    }
}