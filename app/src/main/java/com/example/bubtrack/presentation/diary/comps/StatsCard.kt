package com.example.bubtrack.presentation.diary.comps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun StatsCard(
    modifier: Modifier = Modifier,
    statsCardItem: StatsCardItem,
    width: Int = 80,
    height: Int = 80
) {
    Column(
        modifier = modifier
            .width(width.dp)
            .height(height.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = modifier
                .size(25.dp)
                .clip(CircleShape)
                .background(statsCardItem.bgColor),
            contentAlignment = Alignment.Center
        ){
            Icon(
                painter = painterResource(statsCardItem.icon),
                contentDescription = null,
                modifier = modifier.size(10.dp),
                tint = Color.White

            )
        }
        Text(
            statsCardItem.title,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6B7280)
        )
        Text(
            text = "${statsCardItem.value.roundToInt()} ${statsCardItem.unit}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

data class StatsCardItem(
    val title : String,
    val value : Double,
    val icon : Int,
    val unit : String,
    val bgColor : Color
)

