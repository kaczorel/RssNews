package pl.edu.pja.s27599.rssnews.data.local

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val USERS_COLLECTION = "users"
    private val READ_ARTICLES_SUBCOLLECTION = "readArticles"
    private val FAVORITE_ARTICLES_SUBCOLLECTION = "favoriteArticles"

    suspend fun markArticleAsRead(userId: String, articleId: String): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(READ_ARTICLES_SUBCOLLECTION)
                .document(articleId)
                .set(mapOf("timestamp" to System.currentTimeMillis()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addFavoriteArticle(userId: String, articleId: String, articleData: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(FAVORITE_ARTICLES_SUBCOLLECTION)
                .document(articleId)
                .set(articleData)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFavoriteArticle(userId: String, articleId: String): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(FAVORITE_ARTICLES_SUBCOLLECTION)
                .document(articleId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReadArticleIds(userId: String): Result<List<String>> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(READ_ARTICLES_SUBCOLLECTION)
                .get()
                .await()
            val readIds = snapshot.documents.map { it.id }
            Result.success(readIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavoriteArticles(userId: String): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(FAVORITE_ARTICLES_SUBCOLLECTION)
                .get()
                .await()
            val favoriteArticles = snapshot.documents.map { it.data as Map<String, Any> }
            Result.success(favoriteArticles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}