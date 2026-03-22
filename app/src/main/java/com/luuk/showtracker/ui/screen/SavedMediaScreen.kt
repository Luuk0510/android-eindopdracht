package com.luuk.showtracker.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.luuk.showtracker.data.model.TmdbMediaItem
import com.luuk.showtracker.ui.viewmodel.MediaViewModel

@Composable
fun SavedMediaScreen(
    viewModel: MediaViewModel,
    searchQuery: String,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val savedItems by viewModel.savedItems.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val watchedIds by viewModel.watchedIds.collectAsState()
    val configuration = LocalConfiguration.current
    val columnCount = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        SavedMediaScreenDefaults.LandscapeColumnCount
    } else {
        SavedMediaScreenDefaults.PortraitColumnCount
    }
    val shownItems = savedItems.filter { item ->
        val mediaTitle = item.title ?: item.name ?: ""
        mediaTitle.contains(searchQuery, ignoreCase = true)
    }

    SavedMediaContent(
        savedItems = savedItems,
        shownItems = shownItems,
        columnCount = columnCount,
        modifier = modifier,
        isWatched = { itemId -> watchedIds.contains(itemId) },
        ratingBadge = { itemId -> reviews[itemId]?.rating?.toString() },
        onItemClick = onItemClick
    )
}

@Composable
private fun SavedMediaContent(
    savedItems: List<TmdbMediaItem>,
    shownItems: List<TmdbMediaItem>,
    columnCount: Int,
    modifier: Modifier = Modifier,
    isWatched: (Int) -> Boolean,
    ratingBadge: (Int) -> String?,
    onItemClick: (Int) -> Unit
) {
    when {
        savedItems.isEmpty() -> {
            WatchlistMessage(
                text = "No watchlist items yet.",
                modifier = modifier
            )
        }

        shownItems.isEmpty() -> {
            WatchlistMessage(
                text = "No results found.",
                modifier = modifier
            )
        }

        else -> {
            WatchlistGrid(
                shownItems = shownItems,
                columnCount = columnCount,
                modifier = modifier,
                isWatched = isWatched,
                ratingBadge = ratingBadge,
                onItemClick = onItemClick
            )
        }
    }
}

@Composable
private fun WatchlistMessage(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(SavedMediaScreenDefaults.ScreenPadding)
        )
    }
}

@Composable
private fun WatchlistGrid(
    shownItems: List<TmdbMediaItem>,
    columnCount: Int,
    modifier: Modifier = Modifier,
    isWatched: (Int) -> Boolean,
    ratingBadge: (Int) -> String?,
    onItemClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columnCount),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SavedMediaScreenDefaults.GridOuterPadding),
        contentPadding = PaddingValues(SavedMediaScreenDefaults.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(SavedMediaScreenDefaults.GridSpacing),
        horizontalArrangement = Arrangement.spacedBy(SavedMediaScreenDefaults.GridSpacing)
    ) {
        items(shownItems, key = { it.id }) { item ->
            MediaItemRow(
                item = item,
                isWatched = isWatched(item.id),
                ratingBadge = ratingBadge(item.id),
                onClick = { onItemClick(item.id) }
            )
        }
    }
}

private object SavedMediaScreenDefaults {
    const val PortraitColumnCount = 2
    const val LandscapeColumnCount = 3

    val GridOuterPadding = 4.dp
    val GridSpacing = 16.dp
    val ScreenPadding = 16.dp
}
