package com.luuk.showtracker.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.luuk.showtracker.data.model.genreNames
import com.luuk.showtracker.data.model.TmdbMediaItem
import com.luuk.showtracker.ui.component.CompactPrimaryButton
import com.luuk.showtracker.ui.component.ProfileAvatar
import com.luuk.showtracker.ui.screen.MediaDetailScreen
import com.luuk.showtracker.ui.screen.SavedMediaScreen
import com.luuk.showtracker.ui.screen.TrendingMediaScreen
import com.luuk.showtracker.ui.viewmodel.MediaViewModel
import java.io.File
import java.io.FileOutputStream
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun ShowTrackerApp(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isTopLevelScreen =
        currentDestination?.route == Screen.Home.route || currentDestination?.route == Screen.Saved.route
    var searchText by remember { mutableStateOf("") }
    var showSearchField by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var profileName by remember(profile.name) { mutableStateOf(profile.name) }
    var profilePhotoUri by remember(profile.photoUri) { mutableStateOf(profile.photoUri) }

    val cameraPreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            profilePhotoUri = saveProfilePhoto(context, bitmap)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraPreviewLauncher.launch(null)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            profilePhotoUri = uri.toString()
        }
    }

    LaunchedEffect(currentDestination?.route) {
        showSearchField = false
        searchText = ""
    }

    LaunchedEffect(showProfileDialog, profile.name, profile.photoUri) {
        if (showProfileDialog) {
            profileName = profile.name
            profilePhotoUri = profile.photoUri
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (isTopLevelScreen) {
                ShowTrackerTopBar(
                    profileName = profile.name,
                    profilePhotoUri = profile.photoUri,
                    searchText = searchText,
                    showSearchField = showSearchField,
                    onSearchTextChanged = { searchText = it },
                    onProfileClick = { showProfileDialog = true },
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
                        icon = {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = null,
                                modifier = Modifier.size(NavGraphDefaults.NavigationIconSize)
                            )
                        },
                        label = { Text("Trending") },
                        selected = currentDestination.hierarchy.any { it.route == Screen.Home.route },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(
                                alpha = NavGraphDefaults.SELECTED_ITEM_INDICATOR_ALPHA
                            )
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
                        icon = {
                            Icon(
                                Icons.Default.Bookmark,
                                contentDescription = null,
                                modifier = Modifier.size(NavGraphDefaults.NavigationIconSize)
                            )
                        },
                        label = { Text("Watchlist") },
                        selected = currentDestination.hierarchy.any { it.route == Screen.Saved.route },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(
                                alpha = NavGraphDefaults.SELECTED_ITEM_INDICATOR_ALPHA
                            )
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

        if (showProfileDialog) {
            ProfileDialog(
                profileName = profileName,
                profilePhotoUri = profilePhotoUri,
                onProfileNameChange = { profileName = it },
                onChoosePhotoClick = {
                    imagePickerLauncher.launch(arrayOf("image/*"))
                },
                onTakePhotoClick = {
                    val hasCameraPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasCameraPermission) {
                        cameraPreviewLauncher.launch(null)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                onDismiss = { showProfileDialog = false },
                onSave = {
                    viewModel.saveProfile(profileName.trim(), profilePhotoUri)
                    showProfileDialog = false
                }
            )
        }
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

@Composable
private fun ShowTrackerTopBar(
    profileName: String,
    profilePhotoUri: String?,
    searchText: String,
    showSearchField: Boolean,
    onSearchTextChanged: (String) -> Unit,
    onProfileClick: () -> Unit,
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
                .padding(
                    horizontal = NavGraphDefaults.TopBarHorizontalPadding,
                    vertical = NavGraphDefaults.TopBarVerticalPadding
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalMovies,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(NavGraphDefaults.TopBarIconSize)
                )
                Spacer(modifier = Modifier.padding(horizontal = NavGraphDefaults.TopBarTitleSpacing))
                Text(
                    text = "ShowTracker",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                ProfileAvatar(
                    name = profileName,
                    photoUri = profilePhotoUri,
                    modifier = Modifier
                        .clickable(onClick = onProfileClick)
                        .padding(end = NavGraphDefaults.ProfileSpacing)
                        .size(NavGraphDefaults.ProfileAvatarSize)
                )
                Icon(
                    imageVector = if (showSearchField) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White,
                    modifier = Modifier
                        .clickable(onClick = onSearchClick)
                        .padding(NavGraphDefaults.SearchIconPadding)
                        .size(NavGraphDefaults.TopBarIconSize)
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
                        .padding(top = NavGraphDefaults.SearchFieldTopPadding)
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

private fun saveProfilePhoto(
    context: android.content.Context,
    bitmap: Bitmap
): String? {
    return runCatching {
        val photoFile = File(context.filesDir, NavGraphDefaults.PROFILE_PHOTO_FILE_NAME)
        FileOutputStream(photoFile).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, NavGraphDefaults.PROFILE_PHOTO_QUALITY, outputStream)
        }
        Uri.fromFile(photoFile).toString()
    }.getOrNull()
}

@Composable
private fun ProfileDialog(
    profileName: String,
    profilePhotoUri: String?,
    onProfileNameChange: (String) -> Unit,
    onChoosePhotoClick: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = NavGraphDefaults.ProfileDialogOuterPadding),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(NavGraphDefaults.ProfileDialogInnerPadding)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit profile",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier
                            .clickable(onClick = onDismiss)
                            .size(NavGraphDefaults.ProfileCloseIconSize)
                    )
                }

                Spacer(modifier = Modifier.padding(top = NavGraphDefaults.ProfileDialogHeaderSpacing))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileAvatar(
                        name = profileName,
                        photoUri = profilePhotoUri,
                        modifier = Modifier.size(NavGraphDefaults.ProfileDialogAvatarSize)
                    )

                    Spacer(modifier = Modifier.padding(top = NavGraphDefaults.ProfileDialogSpacing))

                    Text(
                        text = profileName.ifBlank { "Guest" },
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.padding(top = NavGraphDefaults.ProfileDialogSpacing))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(NavGraphDefaults.ProfileDialogSpacing)
                    ) {
                        CompactPrimaryButton(
                            text = "Gallery",
                            onClick = onChoosePhotoClick
                        )

                        CompactPrimaryButton(
                            text = "Camera",
                            onClick = onTakePhotoClick
                        )
                    }
                }

                Spacer(modifier = Modifier.padding(top = NavGraphDefaults.ProfileFieldSpacing))

                OutlinedTextField(
                    value = profileName,
                    onValueChange = onProfileNameChange,
                    label = { Text("Profile name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.padding(top = NavGraphDefaults.ProfileDialogActionsSpacing))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CompactPrimaryButton(
                        text = "Save profile",
                        onClick = onSave
                    )
                }
            }
        }
    }
}

private object NavGraphDefaults {
    const val SELECTED_ITEM_INDICATOR_ALPHA = 0.16f
    const val PROFILE_PHOTO_FILE_NAME = "profile_photo.jpg"
    const val PROFILE_PHOTO_QUALITY = 92

    val NavigationIconSize = 28.dp
    val TopBarIconSize = 28.dp
    val TopBarHorizontalPadding = 16.dp
    val TopBarVerticalPadding = 12.dp
    val TopBarTitleSpacing = 5.dp
    val SearchIconPadding = 6.dp
    val SearchFieldTopPadding = 12.dp
    val ProfileAvatarSize = 34.dp
    val ProfileSpacing = 10.dp
    val ProfileDialogOuterPadding = 20.dp
    val ProfileDialogInnerPadding = 24.dp
    val ProfileDialogAvatarSize = 76.dp
    val ProfileDialogSpacing = 12.dp
    val ProfileDialogHeaderSpacing = 16.dp
    val ProfileFieldSpacing = 18.dp
    val ProfileDialogActionsSpacing = 20.dp
    val ProfileCloseIconSize = 24.dp
}
