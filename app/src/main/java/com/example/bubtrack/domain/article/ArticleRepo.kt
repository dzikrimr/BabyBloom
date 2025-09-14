package com.example.bubtrack.domain.article

import com.example.bubtrack.utill.Resource
import kotlinx.coroutines.flow.Flow

interface ArticleRepo {

    suspend fun getAllArticle() : Flow<Resource<List<Article>>>
    suspend fun getArticleById(id: Int) : Resource<Article>
    suspend fun getArticleByCategory(category: String) : Flow<Resource<List<Article>>>
    suspend fun searchArticle(query: String) : Flow<Resource<List<Article>>>

}