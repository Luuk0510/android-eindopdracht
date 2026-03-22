package com.luuk.showtracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.luuk.showtracker.ui.screen.MediaDetailScreen
import com.luuk.showtracker.ui.screen.MediaListScreen
import com.luuk.showtracker.ui.screen.SavedMoviesScreen
import com.luuk.showtracker.ui.viewmodel.MediaViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        // Home Scherm
        composable(Screen.Home.route) {
            MediaListScreen(
                viewModel = viewModel,
                onItemClick = { item ->
                    // We coderen de tekst zodat spaties en vreemde tekens de URL niet breken
                    val title = URLEncoder.encode(item.title ?: item.name ?: "Unknown", StandardCharsets.UTF_8.toString())
                    val overview = URLEncoder.encode(item.overview, StandardCharsets.UTF_8.toString())
                    val poster = URLEncoder.encode(item.posterPath ?: "", StandardCharsets.UTF_8.toString())
                    
                    navController.navigate(Screen.Details.createRoute(title, overview, poster))
                }
            )
        }

        // Opgeslagen Scherm
        composable(Screen.Saved.route) {
            SavedMoviesScreen()
        }

        // Detail Scherm
        composable(
            route = Screen.Details.route,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("overview") { type = NavType.StringType },
                navArgument("poster") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Hier decoderen we de tekst weer terug naar normaal leesbare tekst
            val title = URLDecoder.decode(backStackEntry.arguments?.getString("title") ?: "", StandardCharsets.UTF_8.toString())
            val overview = URLDecoder.decode(backStackEntry.arguments?.getString("overview") ?: "", StandardCharsets.UTF_8.toString())
            val poster = URLDecoder.decode(backStackEntry.arguments?.getString("poster") ?: "", StandardCharsets.UTF_8.toString())

            MediaDetailScreen(
                title = title,
                overview = overview,
                posterPath = poster,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}