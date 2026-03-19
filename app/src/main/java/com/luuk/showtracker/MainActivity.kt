package com.luuk.showtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.luuk.showtracker.data.api.RetrofitClient
import com.luuk.showtracker.data.repository.MediaRepository
import com.luuk.showtracker.ui.screen.MediaDetailScreen
import com.luuk.showtracker.ui.screen.MediaListScreen
import com.luuk.showtracker.ui.screen.SavedMoviesScreen
import com.luuk.showtracker.ui.theme.ShowTrackerTheme
import com.luuk.showtracker.ui.viewmodel.MediaViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val repository = MediaRepository(RetrofitClient.tmdbService)
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MediaViewModel(repository) as T
            }
        }

        enableEdgeToEdge()
        setContent {
            ShowTrackerTheme {
                val navController = rememberNavController()
                val mediaViewModel: MediaViewModel = viewModel(factory = viewModelFactory)

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        
                        // Only show bottom bar on top-level screens
                        if (currentDestination?.route == "home" || currentDestination?.route == "saved") {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                    label = { Text("Home") },
                                    selected = currentDestination.hierarchy.any { it.route == "home" },
                                    onClick = {
                                        navController.navigate("home") {
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
                                    selected = currentDestination.hierarchy.any { it.route == "saved" },
                                    onClick = {
                                        navController.navigate("saved") {
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
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") { 
                            MediaListScreen(
                                viewModel = mediaViewModel,
                                onItemClick = { item ->
                                    val title = URLEncoder.encode(item.title ?: item.name ?: "Unknown", StandardCharsets.UTF_8.toString())
                                    val overview = URLEncoder.encode(item.overview, StandardCharsets.UTF_8.toString())
                                    val poster = URLEncoder.encode(item.posterPath ?: "", StandardCharsets.UTF_8.toString())
                                    navController.navigate("details/$title/$overview/$poster")
                                }
                            ) 
                        }
                        composable("saved") { 
                            SavedMoviesScreen() 
                        }
                        composable(
                            route = "details/{title}/{overview}/{poster}",
                            arguments = listOf(
                                navArgument("title") { type = NavType.StringType },
                                navArgument("overview") { type = NavType.StringType },
                                navArgument("poster") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            MediaDetailScreen(
                                title = URLDecoder.decode(
                                    backStackEntry.arguments?.getString("title") ?: "",
                                    StandardCharsets.UTF_8.toString()
                                ),
                                overview = URLDecoder.decode(
                                    backStackEntry.arguments?.getString("overview") ?: "",
                                    StandardCharsets.UTF_8.toString()
                                ),
                                posterPath = URLDecoder.decode(
                                    backStackEntry.arguments?.getString("poster") ?: "",
                                    StandardCharsets.UTF_8.toString()
                                ),
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}