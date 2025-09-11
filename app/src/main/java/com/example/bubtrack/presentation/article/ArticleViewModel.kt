package com.example.bubtrack.presentation.article

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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


    private fun getAllArticle(){
        viewModelScope.launch {
            articleRepo.getAllArticle().collect{
                when(it){
                    is Resource.Loading -> {
                        _state.value = ArticleUiState(isLoading = true)
                    }
                    is Resource.Success -> {
                        _state.value = ArticleUiState(articles = it.data ?: emptyList())
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