package pl.edu.pja.s27599.rssnews.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.navigation.NavController
import pl.edu.pja.s27599.rssnews.Screen
import pl.edu.pja.s27599.rssnews.data.model.Article

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListScreen(
    articleListViewModel: ArticleListViewModel = hiltViewModel(),
    onArticleClick: (String, String) -> Unit,
    onSignOut: () -> Unit,
    navController: NavController,
) {
    val articles by articleListViewModel.articles.collectAsState()
    val isLoading by articleListViewModel.isLoading.collectAsState()
    val error by articleListViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        articleListViewModel.fetchArticles()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RSS News") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Favorites.route)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Ulubione artykuły"
                        )
                    }
                    IconButton(onClick = { articleListViewModel.fetchArticles() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh articles")
                    }
                    Button(onClick = onSignOut) {
                        Text("Sign Out")
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

            if (articles.isEmpty() && !isLoading && error == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No articles found. Pull to refresh or check your internet connection.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(articles, key = { it.id }) { article ->
                        ArticleListItem(
                            article = article,
                            onArticleClick = onArticleClick,
                            onToggleFavorite = { articleListViewModel.toggleFavoriteArticle(article) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListItem(
    article: Article,
    onArticleClick: (String, String) -> Unit,
    onToggleFavorite: (Article) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onArticleClick(article.id, article.articleUrl)
            },
        colors = CardDefaults.cardColors(
            containerColor = if (article.isRead) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            article.imageUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = article.title,
                    modifier = Modifier
                        .size(96.dp)
                        .padding(end = 16.dp),
                    contentScale = ContentScale.Crop,

                )
            } ?: run {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .padding(end = 16.dp)
                        .clickable { onArticleClick(article.id, article.articleUrl) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (article.isRead) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = article.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (article.isRead) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 3
                )
            }
            IconButton(onClick = { onToggleFavorite(article) }) {
                Icon(
                    imageVector = if (article.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (article.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

