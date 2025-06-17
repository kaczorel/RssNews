package pl.edu.pja.s27599.rssnews.data.model

data class Article(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val articleUrl: String,
    var isRead: Boolean = false,
    var isFavorite: Boolean = false
) {
    companion object {
        fun fromRssItem(item: Item): Article {
            val id = item.link.hashCode().toString()
            val imageUrl = item.enclosure?.url ?: extractImageUrlFromDescription(item.description)

            return Article(
                id = id,
                title = item.title ?: "Brak tytułu",
                description = (item.description ?: "").replace(Regex("<img.*>"), ""),
                imageUrl = imageUrl,
                articleUrl = item.link ?: "brak_linku",
                isRead = false,
                isFavorite = false
            )
        }

        private fun extractImageUrlFromDescription(description: String?): String? {
            if (description.isNullOrEmpty()) return null
            val imgTagRegex = "<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>".toRegex()
            val matchResult = imgTagRegex.find(description)
            return matchResult?.groups?.get(1)?.value
        }
    }
}