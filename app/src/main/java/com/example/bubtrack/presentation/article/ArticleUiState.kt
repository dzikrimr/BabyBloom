package com.example.bubtrack.presentation.article

import com.example.bubtrack.domain.article.Article

data class ArticleUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val articles: List<Article> = emptyList()
)
