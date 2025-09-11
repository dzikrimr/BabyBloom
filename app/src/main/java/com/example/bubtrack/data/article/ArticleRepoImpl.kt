package com.example.bubtrack.data.article

import com.example.bubtrack.domain.article.Article
import com.example.bubtrack.domain.article.ArticleRepo
import com.example.bubtrack.domain.article.dummyArticle
import com.example.bubtrack.utill.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ArticleRepoImpl @Inject constructor() : ArticleRepo {

    override suspend fun getAllArticle(): Flow<Resource<List<Article>>> {
        return flow {
            emit(Resource.Loading())
            try {
                val articles = dummyArticle
                emit(Resource.Success(articles))
            } catch (e: Exception) {
                emit(Resource.Error(e.message.toString()))
            }
        }
    }

    override suspend fun getArticleById(id: Int): Resource<Article> {
        return try {
            val article = dummyArticle.find { it.id == id }
            if (article != null) {
                Resource.Success(article)
            } else {
                Resource.Error("Article not found")
            }

        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

}