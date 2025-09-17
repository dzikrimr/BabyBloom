package com.example.bubtrack.presentation.article

import com.example.bubtrack.domain.article.Article

data class ArticleUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val allArticles: List<Article> = emptyList(),
    val categoryArticles: List<Article> = emptyList()
)
