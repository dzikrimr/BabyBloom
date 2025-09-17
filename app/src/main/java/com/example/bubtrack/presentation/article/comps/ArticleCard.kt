package com.example.bubtrack.presentation.article.comps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bubtrack.domain.article.Article

@Composable
fun ArticleCard(
    modifier: Modifier = Modifier,
    article: Article,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp))
            .background(Color.White)
            .clickable(
                enabled = true,
                onClick = onClick,
                indication = null,
                interactionSource = null
            )
    ) {
        AsyncImage(
            model = article.imageUrl,
            contentDescription = "article image",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .fillMaxHeight()
                .fillMaxWidth(0.3f)
                .clip(
                    RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp)
                )
        )
        Spacer(modifier.width(8.dp))
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    article.date,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier.height(5.dp))
                Text(
                    article.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    overflow = TextOverflow.Clip
                )
            }
            Text(
                "${article.source} - ${article.readTime} menit dibaca",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
