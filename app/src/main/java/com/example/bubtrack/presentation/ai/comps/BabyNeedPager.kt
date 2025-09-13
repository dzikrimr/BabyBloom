package com.example.bubtrack.presentation.ai.comps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bubtrack.ui.theme.AppBlue

@Composable
fun BabyNeedPager(
    modifier: Modifier = Modifier,
    results: List<Pair<String, Float>>
) {
    val sortedResult = results.sortedByDescending { it.second }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 } // kamu bisa ganti jadi dynamic kalau mau
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) { page ->
            val startIndex = page * 4
            val endIndex = minOf(startIndex + 4, sortedResult.size)
            val pageItems = sortedResult.subList(startIndex, endIndex)

            Column {
                pageItems.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { (label, score) ->
                            NeedCard(
                                label = label,
                                score = score,
                                isActive = sortedResult.first().first == label,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pagerState.pageCount) { index ->
                Box(
                    modifier = Modifier
                        .size(if (pagerState.currentPage == index) 10.dp else 8.dp)
                        .background(
                            color = if (pagerState.currentPage == index) AppBlue else Color.LightGray,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}