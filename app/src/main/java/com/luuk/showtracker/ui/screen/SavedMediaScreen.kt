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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.luuk.showtracker.R
import com.luuk.showtracker.data.model.TmdbMediaItem
import com.luuk.showtracker.data.model.WatchlistSortOption
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
    val watchlistSortOption by viewModel.watchlistSortOption.collectAsState()
    val configuration = LocalConfiguration.current
    val columnCount = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        SavedMediaScreenDefaults.LANDSCAPE_COLUMN_COUNT
    } else {
        SavedMediaScreenDefaults.PORTRAIT_COLUMN_COUNT
    }
    val sortedSavedItems = savedItems.sortedForWatchlist(watchlistSortOption)
    val shownItems = sortedSavedItems.filter { item ->
        val mediaTitle = item.title ?: item.name ?: ""
        mediaTitle.contains(searchQuery, ignoreCase = true)
    }

    SavedMediaContent(
        savedItems = sortedSavedItems,
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
                text = stringResource(R.string.message_no_watchlist_items),
                modifier = modifier
            )
        }

        shownItems.isEmpty() -> {
            WatchlistMessage(
                text = stringResource(R.string.message_no_results),
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
private fun WatchlistMessage(text: String, modifier: Modifier = Modifier) {
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
    const val PORTRAIT_COLUMN_COUNT = 2
    const val LANDSCAPE_COLUMN_COUNT = 4

    val GridOuterPadding = 4.dp
    val GridSpacing = 16.dp
    val ScreenPadding = 16.dp
}

private fun List<TmdbMediaItem>.sortedForWatchlist(sortOption: WatchlistSortOption): List<TmdbMediaItem> {
    return when (sortOption) {
        WatchlistSortOption.NEWEST -> this
        WatchlistSortOption.OLDEST -> this.asReversed()
        WatchlistSortOption.TITLE_ASC -> sortedBy { (it.title ?: it.name ?: "").lowercase() }
        WatchlistSortOption.TITLE_DESC -> sortedByDescending { (it.title ?: it.name ?: "").lowercase() }
    }
}
