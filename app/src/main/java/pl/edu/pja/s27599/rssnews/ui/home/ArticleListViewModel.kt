package pl.edu.pja.s27599.rssnews.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.edu.pja.s27599.rssnews.data.model.Article
import pl.edu.pja.s27599.rssnews.data.repository.ArticleRepository
import pl.edu.pja.s27599.rssnews.data.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class ArticleListViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentUserId: String? = null

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                currentUserId = user?.uid
                fetchArticles()
            }
        }
    }

    fun fetchArticles() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            articleRepository.getArticles(currentUserId)
                .onSuccess { fetchedArticles ->
                    _articles.value = fetchedArticles
                    _isLoading.value = false
                }
                .onFailure { e ->
                    _error.value = "Failed to load articles: ${e.message}"
                    _isLoading.value = false
                }
        }
    }

    fun markArticleAsRead(article: Article) {
        val userId = currentUserId ?: run {
            _error.value = "User not logged in to mark article as read."
            return
        }

        viewModelScope.launch {
            val result = articleRepository.markArticleAsRead(userId, article.id)
            if (result.isSuccess) {
                _articles.value = _articles.value.map {
                    if (it.id == article.id) it.copy(isRead = true) else it
                }
            } else {
                _error.value = "Failed to mark article as read: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun toggleFavoriteArticle(article: Article) {
        val userId = currentUserId ?: run {
            _error.value = "User not logged in to mark article as favorite."
            return
        }

        viewModelScope.launch {
            val result = articleRepository.toggleFavoriteArticle(userId, article)
            if (result.isSuccess) {
                _articles.value = _articles.value.map {
                    if (it.id == article.id) it.copy(isFavorite = !it.isFavorite) else it
                }
            } else {
                _error.value = "Failed to toggle favorite status: ${result.exceptionOrNull()?.message}"
            }
        }
    }
}