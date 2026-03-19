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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.luuk.showtracker.data.api.RetrofitClient
import com.luuk.showtracker.data.repository.MediaRepository
import com.luuk.showtracker.ui.screen.MediaListScreen
import com.luuk.showtracker.ui.screen.SavedMoviesScreen
import com.luuk.showtracker.ui.theme.ShowTrackerTheme
import com.luuk.showtracker.ui.viewmodel.MediaViewModel

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
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                label = { Text("Home") },
                                selected = currentDestination?.hierarchy?.any { it.route == "home" } == true,
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
                                selected = currentDestination?.hierarchy?.any { it.route == "saved" } == true,
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
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") { 
                            MediaListScreen(viewModel = mediaViewModel) 
                        }
                        composable("saved") { 
                            SavedMoviesScreen() 
                        }
                    }
                }
            }
        }
    }
}