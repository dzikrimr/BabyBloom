package com.example.bubtrack.presentation.article

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bubtrack.R
import com.example.bubtrack.domain.article.dummyArticle
import com.example.bubtrack.ui.theme.AppBackground

@Composable
fun ArticleDetailScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val article = dummyArticle[0]
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back),
            contentDescription = "back button",
            modifier = modifier
                .size(15.dp)
                .clickable(
                    enabled = true,
                    onClick = { navController.popBackStack() },
                    indication = null,
                    interactionSource = null
                )
        )
        Spacer(modifier.height(12.dp))
        AsyncImage(
            model = article.imageUrl,
            contentDescription = "article image",
            contentScale = ContentScale.Crop,
            modifier = modifier.fillMaxWidth().height(220.dp)
        )
        Spacer(modifier.height(12.dp))
        Text(
            article.date,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier.height(12.dp))
        Text(
            article.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        Spacer(modifier.height(12.dp))
        Text(
            "${article.source} - ${article.readTime} menit dibaca",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier.height(12.dp))
        Text(
            article.content,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
