package com.luuk.showtracker.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.luuk.showtracker.data.model.TmdbMediaItem
import com.luuk.showtracker.data.model.genreNames
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
    val profile by viewModel.profile.collectAsState()
    val watchlistSortOption by viewModel.watchlistSortOption.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isTopLevelScreen =
        currentDestination?.route == Screen.Home.route || currentDestination?.route == Screen.Saved.route
    val isSavedScreen = currentDestination?.route == Screen.Saved.route
    var searchText by remember { mutableStateOf("") }
    var showSearchField by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

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
                    showSortButton = isSavedScreen,
                    sortLabel = stringResource(watchlistSortOption.labelResId()),
                    onSortClick = { showSortDialog = true },
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
                ShowTrackerBottomBar(
                    currentDestination = currentDestination,
                    profileName = profile.name,
                    profilePhotoUri = profile.photoUri,
                    onTrendingClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onWatchlistClick = {
                        navController.navigate(Screen.Saved.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onProfileClick = { showProfileDialog = true }
                )
            }
        }
    ) { innerPadding ->
        SetupAppNavigation(
            navController = navController,
            viewModel = viewModel,
            searchText = searchText,
            modifier = Modifier.padding(innerPadding)
        )

        ProfileDialogHost(
            profileName = profile.name,
            profilePhotoUri = profile.photoUri,
            showProfileDialog = showProfileDialog,
            onDismiss = { showProfileDialog = false },
            onSave = { name, photoUri ->
                viewModel.saveProfile(name, photoUri)
                showProfileDialog = false
            }
        )

        if (showSortDialog) {
            WatchlistSortDialog(
                selectedSortOption = watchlistSortOption,
                onOptionSelected = { sortOption ->
                    viewModel.setWatchlistSortOption(sortOption)
                    showSortDialog = false
                },
                onDismiss = { showSortDialog = false }
            )
        }
    }
}

@Composable
fun SetupAppNavigation(
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
                navArgument("genres") { type = NavType.StringType },
                navArgument("releaseDate") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val savedItems by viewModel.savedItems.collectAsState()
            val profile by viewModel.profile.collectAsState()
            val reviews by viewModel.reviews.collectAsState()
            val watchedIds by viewModel.watchedIds.collectAsState()
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
            val releaseDate = URLDecoder.decode(
                backStackEntry.arguments?.getString("releaseDate") ?: "",
                StandardCharsets.UTF_8.toString()
            )
            val mediaItem = TmdbMediaItem(
                id = itemId,
                title = title,
                name = null,
                overview = overview,
                releaseDate = releaseDate.ifBlank { null },
                posterPath = poster.ifBlank { null }
            )

            MediaDetailScreen(
                title = title,
                overview = overview,
                posterPath = mediaItem.posterPath,
                genreNames = genres.split("|").filter { it.isNotBlank() },
                releaseDate = mediaItem.releaseDate,
                isSaved = savedItems.any { it.id == itemId },
                isWatched = watchedIds.contains(itemId),
                profileName = profile.name,
                profilePhotoUri = profile.photoUri,
                currentReview = reviews[itemId],
                onReviewSaved = { reviewTitle, reviewText, rating ->
                    viewModel.saveReview(itemId, reviewTitle, reviewText, rating)
                },
                onReviewDeleted = { viewModel.deleteReview(itemId) },
                onSaveClick = { viewModel.toggleSaved(mediaItem) },
                onWatchedToggle = { viewModel.toggleWatched(itemId) },
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
    val genres = URLEncoder.encode(item.genreNames().joinToString("|"), StandardCharsets.UTF_8.toString())
    val releaseDate = URLEncoder.encode(item.releaseDate ?: "", StandardCharsets.UTF_8.toString())

    navController.navigate(
        Screen.Details.createRoute(item.id, title, overview, poster, genres, releaseDate)
    )
}
