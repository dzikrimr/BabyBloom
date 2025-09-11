package com.example.bubtrack.presentation.article

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bubtrack.R
import com.example.bubtrack.domain.article.dummyArticle
import com.example.bubtrack.presentation.article.comps.ArticleCard
import com.example.bubtrack.presentation.article.comps.ArticleHeaderCard
import com.example.bubtrack.presentation.article.comps.CategoryButton
import com.example.bubtrack.presentation.article.comps.CustomTextField
import com.example.bubtrack.presentation.navigation.ArticleDetailRoute
import com.example.bubtrack.ui.theme.AppBackground
import com.example.bubtrack.ui.theme.BubTrackTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {

    val categoryList = listOf(
        "Semua", "Nutrisi", "Perkembangan", "Kesehatan", "Tips"
    )
    val searchQuery by remember {
        mutableStateOf("")
    }

    var selectedCategory by remember {
        mutableStateOf("Semua")
    }
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(AppBackground)
            .verticalScroll(scrollState)

    ) {
        CustomTextField(
            value = searchQuery,
            onValueChange = {},
            onClear = {},
            onSearch = {},
            modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Spacer(modifier.height(14.dp))
        LazyRow(
            modifier = modifier.fillMaxWidth().padding(start = 16.dp)
        ) {
            items(categoryList.size) {
                CategoryButton(
                    category = categoryList[it],
                    isSelected = selectedCategory == categoryList[it],
                )
                Spacer(modifier.width(8.dp))
            }
        }
        Spacer(modifier.height(25.dp))
        Column(
            modifier.padding(start = 16.dp)
        ) {
            Text(
                "Berita Terbaru",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier.height(8.dp))
            LazyRow(
                modifier = modifier.fillMaxWidth()
            ) {
                items(dummyArticle.size) {
                    ArticleHeaderCard(
                        article = dummyArticle[it],
                        onClick = { navController.navigate(ArticleDetailRoute) }
                    )
                    Spacer(modifier.width(16.dp))
                }
            }
        }
        Spacer(modifier.height(8.dp))
        Column(
            modifier.fillMaxSize().padding(16.dp)
        ) {
            Text(
                "Rekomendasi Berita",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier.height(8.dp))
            dummyArticle.forEach {
                ArticleCard(
                    article = it,
                    onClick = { navController.navigate(ArticleDetailRoute) }
                )
                Spacer(modifier.height(16.dp))
            }
        }
    }
}


