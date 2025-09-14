package com.example.bubtrack.presentation.home.comps

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bubtrack.ui.theme.AppBlue

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BabyInfoCard(
    babyAge: String,
    currentImageIndex: Int,
    images: List<Int>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(
                horizontal = 20.dp,
                vertical = 0.dp
            )
            .padding(bottom = 18.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(color = AppBlue)
            .padding(horizontal = 14.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Bayimu Sekarang",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                babyAge,
                style = MaterialTheme.typography.bodyMedium
            )
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF93C5FD))
                    .padding(vertical = 6.dp, horizontal = 8.dp)
            ) {
                Text(
                    "Growing Well",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
        }
        AnimatedContent(
            targetState = images[currentImageIndex],
            transitionSpec = {
                slideInVertically(animationSpec = tween(500)) { height -> height } with
                        slideOutVertically(animationSpec = tween(500)) { height -> -height }
            },
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
        ) { imageRes ->
            key(imageRes) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    Icon(
                        painter = painterResource(imageRes),
                        contentDescription = "Carousel image",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}