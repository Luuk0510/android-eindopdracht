package com.luuk.showtracker.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.luuk.showtracker.data.model.MediaReview
import com.luuk.showtracker.data.model.genreNames
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isTopLevelScreen =
        currentDestination?.route == Screen.Home.route || currentDestination?.route == Screen.Saved.route
    var searchText by remember { mutableStateOf("") }
    var showSearchField by remember { mutableStateOf(false) }

    LaunchedEffect(currentDestination?.route) {
        showSearchField = false
        searchText = ""
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (isTopLevelScreen) {
                ShowTrackerTopBar(
                    searchText = searchText,
                    showSearchField = showSearchField,
                    onSearchTextChanged = { searchText = it },
                    onSearchClick = {
                        if (showSearchField) {
                            showSearchField = false
                            searchText = ""
                        } else {
                            showSearchField = true
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (isTopLevelScreen) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Trending") },
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
            searchText = searchText,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    viewModel: MediaViewModel,
    searchText: String,
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
                searchQuery = searchText,
                onItemClick = { item -> navigateToDetails(navController, item) }
            )
        }

        composable(Screen.Saved.route) {
            SavedMediaScreen(
                viewModel = viewModel,
                searchQuery = searchText,
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
                navArgument("poster") { type = NavType.StringType },
                navArgument("genres") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val savedItems by viewModel.savedItems.collectAsState()
            val reviews by viewModel.reviews.collectAsState()
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
            val genres = URLDecoder.decode(
                backStackEntry.arguments?.getString("genres") ?: "",
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
                genreNames = genres.split("|").filter { it.isNotBlank() },
                isSaved = savedItems.any { it.id == itemId },
                currentReview = reviews[itemId],
                onReviewSaved = { reviewTitle, reviewText, rating ->
                    viewModel.saveReview(itemId, reviewTitle, reviewText, rating)
                },
                onReviewDeleted = { viewModel.deleteReview(itemId) },
                onSaveClick = { viewModel.toggleSaved(mediaItem) },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun ShowTrackerTopBar(
    searchText: String,
    showSearchField: Boolean,
    onSearchTextChanged: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(showSearchField) {
        if (showSearchField) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalMovies,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                Text(
                    text = "ShowTracker",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (showSearchField) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White,
                    modifier = Modifier
                        .clickable(onClick = onSearchClick)
                        .padding(6.dp)
                )
            }

            if (showSearchField) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = onSearchTextChanged,
                    label = { Text("Search by name") },
                    singleLine = true,
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }
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
    val genres = URLEncoder.encode(item.genreNames().joinToString("|"), StandardCharsets.UTF_8.toString())

    navController.navigate(Screen.Details.createRoute(item.id, title, overview, poster, genres))
}
