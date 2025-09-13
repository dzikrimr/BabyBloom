package com.example.bubtrack.presentation.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bubtrack.domain.article.Article
import com.example.bubtrack.domain.article.ArticleRepo
import com.example.bubtrack.utill.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val articleRepo: ArticleRepo
) : ViewModel() {

    private val _state = MutableStateFlow(ArticleUiState())
    val state = _state.asStateFlow()

    private val _detailArticle = MutableStateFlow<Resource<Article>>(Resource.Idle())
    val detailArticle = _detailArticle.asStateFlow()


    init {
        getAllArticle()
    }


    private fun getAllArticle() {
        viewModelScope.launch {
            articleRepo.getAllArticle().collect {
                when (it) {
                    is Resource.Loading -> {
                        _state.value = ArticleUiState(isLoading = true)
                    }

                    is Resource.Success -> {
                        _state.value = ArticleUiState(allArticles = it.data ?: emptyList())
                    }

                    is Resource.Error -> {
                        _state.value = ArticleUiState(error = it.msg)
                    }

                    else -> {

                    }
                }
            }
        }
    }

    fun getArticleByCategory(category: String) {
        viewModelScope.launch {
            val data = _state.value.allArticles.filter {
                it.category == category
            }
            _state.value = _state.value.copy(
                categoryArticles = data
            )
        }
    }

    fun getDetailArticle(id: Int) {
        viewModelScope.launch {
            val data = articleRepo.getArticleById(id)
            _detailArticle.value = data
        }
    }

    fun searchArticle(query: String) {
        viewModelScope.launch {
            articleRepo.searchArticle(query).collect {
                when (it) {
                    is Resource.Loading -> {
                        _state.value = ArticleUiState(isLoading = true)
                    }

                    is Resource.Success -> {
                        _state.value = ArticleUiState(allArticles = it.data ?: emptyList())
                    }

                    is Resource.Error -> {
                        _state.value = ArticleUiState(error = it.msg)
                    }

                    else -> {
                    }
                }
            }

        }
    }
}