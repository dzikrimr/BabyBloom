package com.example.bubtrack.domain.article


data class Article(
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val date: String = "",
    val source: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val readTime: Int = 0
)



