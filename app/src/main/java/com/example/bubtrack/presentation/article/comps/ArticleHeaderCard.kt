package com.example.bubtrack.presentation.article.comps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bubtrack.domain.article.ArticleModel
import com.example.bubtrack.domain.article.dummyArticle
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme

@Composable
fun ArticleHeaderCard(
    modifier: Modifier = Modifier,
    article: ArticleModel,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .height(230.dp)
            .width(235.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(color = Color.White)
            .clickable(
                enabled = true,
                onClick = onClick,
                indication = null,
                interactionSource = null
            ),
        horizontalAlignment = Alignment.Start
    ) {
        AsyncImage(
            model = article.imageUrl,
            contentDescription = "article image",
            modifier = modifier
                .fillMaxWidth()
                .height(120.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier.height(4.dp))
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                article.date,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier.height(16.dp))
            Text(
                article.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                overflow = TextOverflow.Clip
            )
            Spacer(modifier.height(5.dp))
            Text(
                "${article.source} - ${article.readTime} menit dibaca",
                style = MaterialTheme.typography.bodySmall
            )
        }

    }
}

