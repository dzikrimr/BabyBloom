package com.example.bubtrack.data.article

import com.example.bubtrack.domain.article.Article
import com.example.bubtrack.domain.article.ArticleRepo
import com.example.bubtrack.utill.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ArticleRepoImpl @Inject constructor(
    private val firestore : FirebaseFirestore
) : ArticleRepo {

    override suspend fun getAllArticle(): Flow<Resource<List<Article>>> {
        return flow {
            emit(Resource.Loading())
            try {
                val data = firestore.collection("articles").get().await()
                val articles = data.toObjects(Article::class.java)
                emit(Resource.Success(articles))
            } catch (e: Exception) {
                emit(Resource.Error(e.message.toString()))
            }
        }
    }

    override suspend fun getArticleById(id: Int): Resource<Article> {
        try {
            val data = firestore.collection("articles").document(id.toString()).get().await()
            val article = data.toObject(Article::class.java)
            return Resource.Success(article)
        } catch (e: Exception) {
            return Resource.Error(e.message.toString())
        }
    }

    override suspend fun getArticleByCategory(category: String): Flow<Resource<List<Article>>> {
        return flow {
            emit(Resource.Loading())
            try {
                val data = firestore.collection("articles").whereEqualTo("category", category).get().await()
                val articles = data.toObjects(Article::class.java)
                emit(Resource.Success(articles))
            } catch (e: Exception) {
                emit(Resource.Error(e.message.toString()))
            }
        }
    }

}