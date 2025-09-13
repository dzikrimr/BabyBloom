package com.example.bubtrack.presentation.ai.comps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bubtrack.R

@Composable
fun NeedCard(
    modifier: Modifier = Modifier,
    label: String,
    score: Float,
    isActive: Boolean
) {

    val title = when(label){
        "cold_hot" -> "Cold/Hot"
        "belly_pain" -> "Belly Pain"
        else -> {
            label.replaceFirstChar { it.uppercase() }
        }
    }

    val shoudSelect = score > 0f
    val icon = when(label){
        "hungry" -> R.drawable.ic_hungry
        "cold_hot" -> R.drawable.ic_coldhot
        "tired" -> R.drawable.ic_tired
        "belly_pain" -> R.drawable.ic_bellypain
        "discomfort" -> R.drawable.ic_discomfort
        "burping" -> R.drawable.ic_burping
        "laugh" -> R.drawable.ic_laugh
        "silence" -> R.drawable.ic_silence
        else -> R.drawable.ic_speaker
    }
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive && shoudSelect) Color(0xFFFEF2F2) else Color(0xFFF9FAFB)
        ),
        border = if (isActive && shoudSelect) BorderStroke(2.dp, Color(0xFFF87171)) else null
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color = if (isActive && shoudSelect) Color(0xFFF87171) else Color(0xFFD1D5DB))
            ){
                Icon(
                    painter = painterResource(
                        icon
                    ),
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp).align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    fontWeight = if (isActive && shoudSelect) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = "${(score * 100).toInt()}% match",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280)
                )
            }

        }
    }
}