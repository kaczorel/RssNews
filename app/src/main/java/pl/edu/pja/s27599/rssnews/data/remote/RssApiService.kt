package pl.edu.pja.s27599.rssnews.data.remote

import pl.edu.pja.s27599.rssnews.data.model.RssFeed
import retrofit2.http.GET
import retrofit2.http.Url

interface RssApiService {
    @GET
    suspend fun getRssFeed(@Url url: String): RssFeed
}