package com.luuk.showtracker.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.luuk.showtracker.data.model.TmdbMediaItem
import com.luuk.showtracker.ui.screen.MediaDetailScreen
import com.luuk.showtracker.ui.screen.SavedMediaScreen
import com.luuk.showtracker.ui.screen.TrendingMediaScreen
import com.luuk.showtracker.ui.viewmodel.MediaViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun ShowTrackerApp(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            if (
                currentDestination?.route == Screen.Home.route ||
                currentDestination?.route == Screen.Saved.route
            ) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Home") },
                        selected = currentDestination.hierarchy.any { it.route == Screen.Home.route },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                        ),
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                        label = { Text("Saved") },
                        selected = currentDestination.hierarchy.any { it.route == Screen.Saved.route },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                        ),
                        onClick = {
                            navController.navigate(Screen.Saved.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        SetupNavGraph(
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

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
        composable(Screen.Home.route) {
            TrendingMediaScreen(
                viewModel = viewModel,
                onItemClick = { item -> navigateToDetails(navController, item) }
            )
        }

        composable(Screen.Saved.route) {
            SavedMediaScreen(
                viewModel = viewModel,
                onItemClick = { itemId ->
                    val savedItem = viewModel.savedItems.value.firstOrNull { it.id == itemId }
                    if (savedItem != null) {
                        navigateToDetails(navController, savedItem)
                    }
                }
            )
        }

        composable(
            route = Screen.Details.route,
            arguments = listOf(
                navArgument("id") { type = NavType.IntType },
                navArgument("title") { type = NavType.StringType },
                navArgument("overview") { type = NavType.StringType },
                navArgument("poster") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val savedItems by viewModel.savedItems.collectAsState()
            val itemId = backStackEntry.arguments?.getInt("id") ?: 0
            val title = URLDecoder.decode(
                backStackEntry.arguments?.getString("title") ?: "",
                StandardCharsets.UTF_8.toString()
            )
            val overview = URLDecoder.decode(
                backStackEntry.arguments?.getString("overview") ?: "",
                StandardCharsets.UTF_8.toString()
            )
            val poster = URLDecoder.decode(
                backStackEntry.arguments?.getString("poster") ?: "",
                StandardCharsets.UTF_8.toString()
            )
            val mediaItem = TmdbMediaItem(
                id = itemId,
                title = title,
                name = null,
                overview = overview,
                posterPath = poster.ifBlank { null }
            )

            MediaDetailScreen(
                title = title,
                overview = overview,
                posterPath = mediaItem.posterPath,
                isSaved = savedItems.any { it.id == itemId },
                onSaveClick = { viewModel.toggleSaved(mediaItem) },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

private fun navigateToDetails(
    navController: NavHostController,
    item: TmdbMediaItem
) {
    val title = URLEncoder.encode(
        item.title ?: item.name ?: "Unknown",
        StandardCharsets.UTF_8.toString()
    )
    val overview = URLEncoder.encode(item.overview, StandardCharsets.UTF_8.toString())
    val poster = URLEncoder.encode(item.posterPath ?: "", StandardCharsets.UTF_8.toString())

    navController.navigate(Screen.Details.createRoute(item.id, title, overview, poster))
}
