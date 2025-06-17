package pl.edu.pja.s27599.rssnews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import dagger.hilt.android.AndroidEntryPoint
import pl.edu.pja.s27599.rssnews.ui.auth.AuthScreen
import pl.edu.pja.s27599.rssnews.ui.auth.AuthState
import pl.edu.pja.s27599.rssnews.ui.auth.AuthViewModel
import pl.edu.pja.s27599.rssnews.ui.favorites.FavoriteArticlesScreen
import pl.edu.pja.s27599.rssnews.ui.home.ArticleDetailScreen
import pl.edu.pja.s27599.rssnews.ui.home.ArticleListScreen
import pl.edu.pja.s27599.rssnews.ui.theme.RssNewsTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RssNewsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RssNewsApp()
                }
            }
        }
    }
}

@Composable
fun RssNewsApp(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    val startDestination = when (authState) {
        is AuthState.Authenticated -> Screen.ArticleList.route
        else -> Screen.Auth.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Auth.route) {
            AuthScreen(
                authViewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate(Screen.ArticleList.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.ArticleList.route) {
            ArticleListScreen(
                onArticleClick = { articleId, articleUrl ->
                    navController.navigate("${Screen.ArticleDetail.route}/$articleId?url=$articleUrl")
                },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                navController = navController

            )
        }
        composable(
            route = "${Screen.ArticleDetail.route}/{articleId}?url={articleUrl}",
            arguments = listOf(
                navArgument("articleId") { defaultValue = "" },
                navArgument("articleUrl") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId")
            val articleUrl = backStackEntry.arguments?.getString("articleUrl")
            ArticleDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Favorites.route) {
            FavoriteArticlesScreen(
                onArticleClick = { articleId, articleUrl ->
                    navController.navigate("${Screen.ArticleDetail.route}/$articleId?url=$articleUrl")
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object ArticleList : Screen("article_list")
    object ArticleDetail : Screen("article_detail")
    object Favorites : Screen("favorites")
}