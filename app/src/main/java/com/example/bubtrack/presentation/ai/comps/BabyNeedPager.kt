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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bubtrack.ui.theme.AppBlue
import kotlin.math.ceil

@Composable
fun BabyNeedPager(
    modifier: Modifier = Modifier,
    results: List<Pair<String, Float>>
) {
    if (results.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Belum ada hasil analisis.")
        }
        return
    }

    val sortedResult = results.sortedByDescending { it.second }
    val itemsPerPage = 4
    val pageCount = ceil(sortedResult.size.toDouble() / itemsPerPage).toInt()
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pageCount }
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .height(180.dp) // Match CryAnalyzerScreen container height
        ) { page ->
            val startIndex = page * itemsPerPage
            val endIndex = minOf(startIndex + itemsPerPage, sortedResult.size)
            val pageItems = if (startIndex < sortedResult.size) {
                sortedResult.subList(startIndex, endIndex)
            } else {
                emptyList()
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pageItems.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { (label, score) ->
                            NeedCard(
                                label = label,
                                score = score,
                                isActive = sortedResult.firstOrNull()?.first == label,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Add empty placeholders for rows with fewer than 2 items
                        if (rowItems.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
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