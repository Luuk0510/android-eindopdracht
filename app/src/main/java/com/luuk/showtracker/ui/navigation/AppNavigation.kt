package com.luuk.showtracker.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.luuk.showtracker.R
import com.luuk.showtracker.data.model.genreNames
import com.luuk.showtracker.ui.screen.MediaDetailScreen
import com.luuk.showtracker.ui.screen.SavedMediaScreen
import com.luuk.showtracker.ui.screen.TrendingMediaScreen
import com.luuk.showtracker.ui.viewmodel.MediaViewModel

@Composable
fun ShowTrackerApp(viewModel: MediaViewModel, modifier: Modifier = Modifier) {
    val profile by viewModel.profile.collectAsState()
    val savedItems by viewModel.savedItems.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val watchlistSortOption by viewModel.watchlistSortOption.collectAsState()
    val watchedIds by viewModel.watchedIds.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isTopLevelScreen = currentDestination?.route == Screen.Home.route || currentDestination?.route == Screen.Saved.route
    val isSavedScreen = currentDestination?.route == Screen.Saved.route
    var searchText by remember { mutableStateOf("") }
    var showSearchField by remember { mutableStateOf(false) }
    val showProfileDialogState = remember { mutableStateOf(false) }
    val showSortDialogState = remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(currentDestination?.route) {
        showSearchField = false
        searchText = ""
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collect { messageResId ->
            snackbarHostState.showSnackbar(
                message = context.getString(messageResId),
                withDismissAction = true
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                Snackbar(
                    shape = RoundedCornerShape(18.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    dismissAction = {
                        IconButton(onClick = { snackbarData.dismiss() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.content_close),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                ) {
                    androidx.compose.material3.Text(
                        text = snackbarData.visuals.message,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        topBar = {
            if (isTopLevelScreen) {
                ShowTrackerTopBar(
                    searchText = searchText,
                    showSearchField = showSearchField,
                    onSearchTextChanged = { searchText = it },
                    showSortButton = isSavedScreen,
                    sortLabel = stringResource(watchlistSortOption.labelResId()),
                    onSortClick = { showSortDialogState.value = true },
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
                    onProfileClick = { showProfileDialogState.value = true }
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
            savedCount = savedItems.size,
            watchedCount = watchedIds.size,
            reviewCount = reviews.size,
            showProfileDialog = showProfileDialogState.value,
            onDismiss = { showProfileDialogState.value = false },
            onSave = { name, photoUri ->
                viewModel.saveProfile(name, photoUri)
                showProfileDialogState.value = false
            }
        )

        if (showSortDialogState.value) {
            WatchlistSortDialog(
                selectedSortOption = watchlistSortOption,
                onOptionSelected = { sortOption ->
                    viewModel.setWatchlistSortOption(sortOption)
                    showSortDialogState.value = false
                },
                onDismiss = { showSortDialogState.value = false }
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
                onItemClick = { item ->
                    viewModel.selectMediaItem(item)
                    navigateToDetails(navController, item.id)
                }
            )
        }

        composable(Screen.Saved.route) {
            SavedMediaScreen(
                viewModel = viewModel,
                searchQuery = searchText,
                onItemClick = { itemId ->
                    val mediaItem = viewModel.getMediaItemById(itemId)
                    if (mediaItem != null) {
                        viewModel.selectMediaItem(mediaItem)
                        navigateToDetails(navController, itemId)
                    }
                }
            )
        }

        composable(
            route = Screen.Details.route,
            arguments = listOf(
                navArgument("id") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val savedItems by viewModel.savedItems.collectAsState()
            val profile by viewModel.profile.collectAsState()
            val reviews by viewModel.reviews.collectAsState()
            val watchedIds by viewModel.watchedIds.collectAsState()
            val itemId = backStackEntry.arguments?.getInt("id") ?: 0
            val mediaItem = viewModel.getMediaItemById(itemId)

            if (mediaItem == null) {
                LaunchedEffect(itemId) {
                    navController.popBackStack()
                }
                return@composable
            }

            MediaDetailScreen(
                title = mediaItem.title ?: mediaItem.name.orEmpty(),
                overview = mediaItem.overview,
                posterPath = mediaItem.posterPath,
                genreNames = mediaItem.genreNames(),
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

private fun navigateToDetails(navController: NavHostController, itemId: Int) {
    navController.navigate(Screen.Details.createRoute(itemId))
}
