package com.example.bubtrack.presentation.article.comps
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bubtrack.ui.theme.AppPurple

@Composable
fun CategoryButton(
    modifier: Modifier = Modifier,
    category: String,
    isSelected : Boolean
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(color = if (isSelected) AppPurple else Color.White)
            .border(
                width = 1.dp,
                color = AppPurple,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(
                vertical = 8.dp,
                horizontal = 12.dp
            )
    ) {
        Text(
            modifier = modifier.align(Alignment.Center),
            text = category,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) Color.White else AppPurple
        )
    }
}