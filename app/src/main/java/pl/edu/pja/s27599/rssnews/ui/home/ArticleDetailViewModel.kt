package pl.edu.pja.s27599.rssnews.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pl.edu.pja.s27599.rssnews.data.repository.ArticleRepository
import pl.edu.pja.s27599.rssnews.data.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val articleId: String? = savedStateHandle["articleId"]
    private val articleUrl: String? = savedStateHandle["articleUrl"]

    fun getArticleUrl(): String? = articleUrl

    fun markArticleAsRead() {
        val userId = authRepository.currentUser.value?.uid ?: return
        val id = articleId ?: return

        viewModelScope.launch {
            articleRepository.markArticleAsRead(userId, id)
        }
    }
}