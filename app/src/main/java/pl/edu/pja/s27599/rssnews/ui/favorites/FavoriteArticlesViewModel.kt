package pl.edu.pja.s27599.rssnews.ui.favorites

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
class FavoriteArticlesViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _favoriteArticles = MutableStateFlow<List<Article>>(emptyList())
    val favoriteArticles: StateFlow<List<Article>> = _favoriteArticles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentUserId: String? = null

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                currentUserId = user?.uid
                if (user != null) {
                    fetchFavoriteArticles()
                } else {
                    _favoriteArticles.value = emptyList()
                }
            }
        }
    }

    fun fetchFavoriteArticles() {
        val userId = currentUserId ?: run {
            _error.value = "User not logged in to view favorite articles."
            _favoriteArticles.value = emptyList()
            return
        }

        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            articleRepository.getFavoriteArticles(userId)
                .onSuccess { articles ->
                    _favoriteArticles.value = articles
                    _isLoading.value = false
                }
                .onFailure { e ->
                    _error.value = "Failed to load favorite articles: ${e.message}"
                    _isLoading.value = false
                }
        }
    }

    fun toggleFavoriteArticle(article: Article) {
        val userId = currentUserId ?: run {
            _error.value = "User not logged in to remove favorite article."
            return
        }

        viewModelScope.launch {
            val result = articleRepository.toggleFavoriteArticle(userId, article)
            if (result.isSuccess) {
                fetchFavoriteArticles()
            } else {
                _error.value = "Failed to toggle favorite status: ${result.exceptionOrNull()?.message}"
            }
        }
    }
}