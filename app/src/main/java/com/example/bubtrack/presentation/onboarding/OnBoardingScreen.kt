package com.example.bubtrack.presentation.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bubtrack.R
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme
import kotlinx.coroutines.launch

@Composable
fun OnBoardingScreen(
    modifier: Modifier = Modifier,
    navigateLogin : () -> Unit
) {

    val onBoardingItems = listOf(
        OnBoardingItem(
            img = R.drawable.img_onboard1,
            title = "Baby Bloom",
            desc = "Pantau perkembangan Buah Hati Anda dengan mudah dan menyenangkan."
        ),
        OnBoardingItem(
            img = R.drawable.img_onboard2,
            title = "Pemantauan Bayi dengan AI",
            desc = "Deteksi wajah, lacak gerakan, peringatan saat bayi berguling, dan analisis tangisan — semua dalam satu aplikasi."
        ),
        OnBoardingItem(
            img = R.drawable.img_onboard3,
            title = "Pantau Setiap Perkembangan",
            desc = "Catat momen berharga, awasi pertumbuhan berat badan, dan simpan jejak perjalanan si kecil dalam buku harian digital."
        )
    )

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = {
            onBoardingItems.size
        }
    )
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Image(
                    painter = painterResource(
                        id = onBoardingItems[it].img
                    ),
                    contentDescription = null,
                    modifier = modifier.size(200.dp)
                )
                Text(
                    text = onBoardingItems[it].title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (it == 0) 30.sp else 28.sp
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = onBoardingItems[it].desc,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color(0xFF4B5563),
                    ),
                    textAlign = TextAlign.Center,
                    modifier = modifier.padding(horizontal = 12.dp)
                )
            }
        }
        Spacer(modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Lewati",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = AppPurple,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.clickable {
                    navigateLogin()
                }
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(onBoardingItems.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 12.dp else 10.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) AppPurple else Color.LightGray)
                    )
                }
            }

            IconButton(
                onClick = {
                    if (pagerState.currentPage < onBoardingItems.size - 1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage+1)
                        }
                    } else {
                        navigateLogin()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next",
                    tint = AppPurple,
                    modifier = modifier.size(30.dp)
                )
            }

        }
    }

}

data class OnBoardingItem(
    val img: Int,
    val title: String,
    val desc: String,
)

@Preview
@Composable
private fun OnBoardingScreenPreview() {
    BubTrackTheme {
        OnBoardingScreen(navigateLogin = {})
    }

}
