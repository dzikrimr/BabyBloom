package com.example.bubtrack

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bubtrack.ui.theme.BubTrackTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onAnimationComplete: () -> Unit = {}
) {
    var showLogo by remember { mutableStateOf(false) }
    var showStar1 by remember { mutableStateOf(false) }
    var showStar2 by remember { mutableStateOf(false) }
    var showStar3 by remember { mutableStateOf(false) }

    val logoAlpha by animateFloatAsState(
        targetValue = if (showLogo) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "logoAlpha"
    )

    val star1Alpha by animateFloatAsState(
        targetValue = if (showStar1) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "star1Alpha"
    )

    val star1Scale by animateFloatAsState(
        targetValue = if (showStar1) 1f else 0.3f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "star1Scale"
    )

    val star2Alpha by animateFloatAsState(
        targetValue = if (showStar2) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "star2Alpha"
    )

    val star2Scale by animateFloatAsState(
        targetValue = if (showStar2) 1f else 0.3f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "star2Scale"
    )

    val star3Alpha by animateFloatAsState(
        targetValue = if (showStar3) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "star3Alpha"
    )

    val star3Scale by animateFloatAsState(
        targetValue = if (showStar3) 1f else 0.3f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "star3Scale"
    )

    LaunchedEffect(Unit) {
        showLogo = true
        delay(800)
        showStar1 = true
        delay(600)
        showStar2 = true
        delay(300)
        showStar3 = true
        delay(600)
        onAnimationComplete()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Logo
            Image(
                painter = painterResource(R.drawable.babybloom_logo),
                contentDescription = "logo",
                modifier = Modifier
                    .size(200.dp)
                    .alpha(logoAlpha)
            )

            // Bintang 1
            Image(
                painter = painterResource(R.drawable.ic_star1),
                contentDescription = "star 1",
                modifier = Modifier
                    .size(30.dp)
                    .offset(x = 50.dp, y = (-50).dp)
                    .alpha(star1Alpha)
                    .scale(star1Scale)
            )

            // Bintang 2
            Image(
                painter = painterResource(R.drawable.ic_star2),
                contentDescription = "star 2",
                modifier = Modifier
                    .size(18.dp)
                    .offset(x = 70.dp, y = (-65).dp)
                    .alpha(star2Alpha)
                    .scale(star2Scale)
            )

            // Bintang 3
            Image(
                painter = painterResource(R.drawable.ic_star3),
                contentDescription = "star 3",
                modifier = Modifier
                    .size(12.dp)
                    .offset(x = 55.dp, y = (-75).dp)
                    .alpha(star3Alpha)
                    .scale(star3Scale)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    BubTrackTheme {
        SplashScreen()
    }
}