package com.luuk.showtracker.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.luuk.showtracker.ui.screen.MediaDetailScreen
import com.luuk.showtracker.ui.screen.MediaListScreen
import com.luuk.showtracker.ui.screen.SavedMoviesScreen
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
            MediaListScreen(
                viewModel = viewModel,
                onItemClick = { item ->
                    val title = URLEncoder.encode(
                        item.title ?: item.name ?: "Unknown",
                        StandardCharsets.UTF_8.toString()
                    )
                    val overview = URLEncoder.encode(item.overview, StandardCharsets.UTF_8.toString())
                    val poster = URLEncoder.encode(item.posterPath ?: "", StandardCharsets.UTF_8.toString())
                    
                    navController.navigate(Screen.Details.createRoute(title, overview, poster))
                }
            )
        }

        composable(Screen.Saved.route) {
            SavedMoviesScreen()
        }

        composable(
            route = Screen.Details.route,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("overview") { type = NavType.StringType },
                navArgument("poster") { type = NavType.StringType }
            )
        ) { backStackEntry ->
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

            MediaDetailScreen(
                title = title,
                overview = overview,
                posterPath = poster,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
