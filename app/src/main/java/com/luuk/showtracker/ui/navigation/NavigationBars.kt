package com.luuk.showtracker.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.luuk.showtracker.R
import com.luuk.showtracker.ui.component.ProfileAvatar

@Composable
internal fun ShowTrackerBottomBar(
    currentDestination: NavDestination?,
    profileName: String,
    profilePhotoUri: String?,
    onTrendingClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Whatshot,
                    contentDescription = null,
                    modifier = Modifier.size(AppNavigationDefaults.NavigationIconSize)
                )
            },
            label = { Text(stringResource(R.string.nav_trending)) },
            selected = currentDestination?.hierarchy?.any { it.route == Screen.Home.route } == true,
            colors = navigationBarItemColors(),
            onClick = onTrendingClick
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Bookmark,
                    contentDescription = null,
                    modifier = Modifier.size(AppNavigationDefaults.NavigationIconSize)
                )
            },
            label = { Text(stringResource(R.string.nav_watchlist)) },
            selected = currentDestination?.hierarchy?.any { it.route == Screen.Saved.route } == true,
            colors = navigationBarItemColors(),
            onClick = onWatchlistClick
        )
        NavigationBarItem(
            icon = {
                ProfileAvatar(
                    name = profileName,
                    photoUri = profilePhotoUri,
                    modifier = Modifier.size(AppNavigationDefaults.NavigationProfileAvatarSize)
                )
            },
            label = { Text(stringResource(R.string.nav_profile)) },
            selected = false,
            colors = navigationBarItemColors(),
            onClick = onProfileClick
        )
    }
}

@Composable
internal fun ShowTrackerTopBar(
    searchText: String,
    showSearchField: Boolean,
    onSearchTextChanged: (String) -> Unit,
    showSortButton: Boolean,
    sortLabel: String,
    onSortClick: () -> Unit,
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
        color = NavigationBarDefaults.containerColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    horizontal = AppNavigationDefaults.TopBarHorizontalPadding,
                    vertical = AppNavigationDefaults.TopBarVerticalPadding
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
                    modifier = Modifier.size(AppNavigationDefaults.TopBarIconSize)
                )
                Spacer(modifier = Modifier.padding(horizontal = AppNavigationDefaults.TopBarTitleSpacing))
                Text(
                    text = stringResource(R.string.app_name),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                if (showSortButton) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(onClick = onSortClick)
                            .padding(end = AppNavigationDefaults.SortButtonEndPadding)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.topbar_sort_watchlist),
                            tint = Color.White,
                            modifier = Modifier.size(AppNavigationDefaults.SortIconSize)
                        )
                        Spacer(modifier = Modifier.padding(horizontal = AppNavigationDefaults.SortLabelSpacing))
                        Text(
                            text = sortLabel,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Icon(
                    imageVector = if (showSearchField) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = stringResource(R.string.topbar_search),
                    tint = Color.White,
                    modifier = Modifier
                        .clickable(onClick = onSearchClick)
                        .padding(AppNavigationDefaults.SearchIconPadding)
                        .size(AppNavigationDefaults.TopBarIconSize)
                )
            }

            if (showSearchField) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = onSearchTextChanged,
                    label = { Text(stringResource(R.string.topbar_search_by_name)) },
                    singleLine = true,
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth()
                        .padding(top = AppNavigationDefaults.SearchFieldTopPadding)
                )
            }
        }
    }
}
