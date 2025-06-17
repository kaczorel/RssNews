package pl.edu.pja.s27599.rssnews.data.repository


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.edu.pja.s27599.rssnews.data.local.FirestoreDataSource
import pl.edu.pja.s27599.rssnews.data.model.Article
import pl.edu.pja.s27599.rssnews.data.model.RssFeed
import pl.edu.pja.s27599.rssnews.data.remote.RssApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepository @Inject constructor(
    private val rssApiService: RssApiService,
    private val firestoreDataSource: FirestoreDataSource
) {

    private val RSS_URLS = listOf(
        "https://wiadomosci.gazeta.pl/pub/rss/wiadomosci_kraj.htm",
        "https://wiadomosci.gazeta.pl/pub/rss/wiadomosci_swiat.htm"
    )

    suspend fun getArticles(userId: String?): Result<List<Article>> = withContext(Dispatchers.IO) {
        try {
            val allArticles = mutableListOf<Article>()
            val readArticleIds = userId?.let { firestoreDataSource.getReadArticleIds(it).getOrNull() } ?: emptyList()
            val favoriteArticlesMap = userId?.let {
                firestoreDataSource.getFavoriteArticles(it).getOrNull()
                    ?.associateBy { it["id"] as String }
            } ?: emptyMap()

            for (url in RSS_URLS) {
                val rssFeed: RssFeed = rssApiService.getRssFeed(url)

                val articlesFromFeed = rssFeed.channel?.item?.map { rssItem ->
                    Article.fromRssItem(rssItem).apply {
                        this.isRead = readArticleIds.contains(this.id)
                        this.isFavorite = favoriteArticlesMap.containsKey(this.id)
                    }
                } ?: emptyList()
                allArticles.addAll(articlesFromFeed)
            }
            Result.success(allArticles.distinctBy { it.id })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markArticleAsRead(userId: String, articleId: String): Result<Unit> {
        return firestoreDataSource.markArticleAsRead(userId, articleId)
    }

    suspend fun toggleFavoriteArticle(userId: String, article: Article): Result<Unit> {
        return if (article.isFavorite) {
            firestoreDataSource.removeFavoriteArticle(userId, article.id)
        } else {
            val articleMap = mapOf(
                "id" to article.id,
                "title" to article.title,
                "description" to article.description,
                "imageUrl" to (article.imageUrl ?: ""),
                "articleUrl" to article.articleUrl
            )
            firestoreDataSource.addFavoriteArticle(userId, article.id, articleMap)
        }
    }

    suspend fun getFavoriteArticles(userId: String): Result<List<Article>> = withContext(Dispatchers.IO) {
        try {
            val favoriteArticleMaps = firestoreDataSource.getFavoriteArticles(userId).getOrThrow()
            val favoriteArticles = favoriteArticleMaps.map { map ->
                Article(
                    id = map["id"] as String,
                    title = map["title"] as String,
                    description = map["description"] as String,
                    imageUrl = map["imageUrl"] as String?,
                    articleUrl = map["articleUrl"] as String,
                    isRead = true,
                    isFavorite = true
                )
            }
            Result.success(favoriteArticles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}