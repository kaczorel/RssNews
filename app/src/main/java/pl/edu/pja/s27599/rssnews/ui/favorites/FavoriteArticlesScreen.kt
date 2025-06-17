package pl.edu.pja.s27599.rssnews.ui.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pl.edu.pja.s27599.rssnews.ui.home.ArticleListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteArticlesScreen(
    favoriteArticlesViewModel: FavoriteArticlesViewModel = hiltViewModel(),
    onArticleClick: (String, String) -> Unit
) {
    val favoriteArticles by favoriteArticlesViewModel.favoriteArticles.collectAsState()
    val isLoading by favoriteArticlesViewModel.isLoading.collectAsState()
    val error by favoriteArticlesViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        favoriteArticlesViewModel.fetchFavoriteArticles()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorite Articles") },
                actions = {
                    IconButton(onClick = { favoriteArticlesViewModel.fetchFavoriteArticles() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh favorites")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (favoriteArticles.isEmpty() && !isLoading && error == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No favorite articles yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = paddingValues,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    items(favoriteArticles, key = { it.id }) { article ->
                        ArticleListItem(
                            article = article.copy(isRead = true, isFavorite = true),
                            onArticleClick = onArticleClick,
                            onToggleFavorite = { favoriteArticlesViewModel.toggleFavoriteArticle(article) }
                        )
                    }
                }
            }
        }
    }
}