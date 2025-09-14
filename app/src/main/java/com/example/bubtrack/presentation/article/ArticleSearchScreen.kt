package com.example.bubtrack.presentation.article

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.bubtrack.R
import com.example.bubtrack.presentation.article.comps.ArticleCard
import com.example.bubtrack.presentation.article.comps.CustomTextField
import com.example.bubtrack.ui.theme.AppBackground
import com.example.bubtrack.ui.theme.AppPurple

@Composable
fun ArticleSearchScreen(
    modifier: Modifier = Modifier,
    searchQuery : String,
    navController: NavController,
    viewModel: ArticleViewModel = hiltViewModel(),
    navigateDetail: (Int) -> Unit
) {
    var query by remember { mutableStateOf(searchQuery) }
    LaunchedEffect(Unit) {
        viewModel.searchArticle(searchQuery)
    }
    val context = LocalContext.current

    val uiState by viewModel.state.collectAsState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = "back button",
                modifier = modifier
                    .size(15.dp)
                    .clickable(
                        enabled = true,
                        onClick = { navController.popBackStack() },
                    )
            )
            CustomTextField(
                value = query,
                onValueChange = { query = it },
                onClear = { query = "" },
                onSearch = {
                    if (searchQuery.isNotEmpty()){
                        viewModel.searchArticle(query)
                    } else {
                        Toast.makeText(context,"Masukkan kata kunci pencarian!",Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier.height(22.dp))
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    color = AppPurple
                )
            }

            uiState.error != null -> {
                Row(
                    modifier = modifier
                        .fillMaxWidth().align(Alignment.CenterHorizontally)
                ) {

                }
                Text(
                    text = uiState.error ?: "Terjadi kesalahan",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            uiState.allArticles.isNotEmpty() -> {
                LazyColumn(
                    modifier = modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.allArticles.size) { index ->
                        ArticleCard(
                            article = uiState.allArticles[index],
                            onClick = {
                                navigateDetail(uiState.allArticles[index].id)
                            }
                        )
                    }
                }
            }
            else -> {
                Row(
                    modifier = modifier
                        .fillMaxSize().align(Alignment.CenterHorizontally)
                ) {

                }
                Text(
                    text = "Hasil pencarian tidak ditemukan untuk: $searchQuery",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}