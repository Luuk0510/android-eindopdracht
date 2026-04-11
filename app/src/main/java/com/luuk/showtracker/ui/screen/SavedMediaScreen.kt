package com.luuk.showtracker.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.luuk.showtracker.R
import com.luuk.showtracker.data.model.SavedFilter
import com.luuk.showtracker.data.model.TmdbMediaItem
import com.luuk.showtracker.data.model.WatchlistSortOption
import com.luuk.showtracker.ui.component.MediaItemCard
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
    var selectedFilter by rememberSaveable { mutableStateOf(SavedFilter.ALL) }
    val configuration = LocalConfiguration.current
    val columnCount = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        SavedMediaScreenDefaults.LANDSCAPE_COLUMN_COUNT
    } else {
        SavedMediaScreenDefaults.PORTRAIT_COLUMN_COUNT
    }
    val sortedSavedItems = savedItems.sortedForWatchlist(watchlistSortOption)
    val filteredSavedItems = sortedSavedItems.filter { item ->
        item.matchesSavedFilter(selectedFilter, watchedIds)
    }
    val shownItems = filteredSavedItems.filter { item ->
        val mediaTitle = item.title ?: item.name ?: ""
        mediaTitle.contains(searchQuery, ignoreCase = true)
    }

    SavedMediaContent(
        savedItems = sortedSavedItems,
        selectedFilter = selectedFilter,
        searchQuery = searchQuery,
        shownItems = shownItems,
        columnCount = columnCount,
        modifier = modifier,
        isWatched = { itemId -> watchedIds.contains(itemId) },
        ratingBadge = { itemId -> reviews[itemId]?.rating?.toString() },
        onFilterSelected = { selectedFilter = it },
        onRemoveClick = { item -> viewModel.toggleSaved(item) },
        onItemClick = onItemClick
    )
}

@Composable
private fun SavedMediaContent(
    savedItems: List<TmdbMediaItem>,
    selectedFilter: SavedFilter,
    searchQuery: String,
    shownItems: List<TmdbMediaItem>,
    columnCount: Int,
    modifier: Modifier = Modifier,
    isWatched: (Int) -> Boolean,
    ratingBadge: (Int) -> String?,
    onFilterSelected: (SavedFilter) -> Unit,
    onRemoveClick: (TmdbMediaItem) -> Unit,
    onItemClick: (Int) -> Unit
) {
    when {
        savedItems.isEmpty() -> {
            SavedStateMessage(
                title = stringResource(R.string.saved_empty_title),
                subtitle = stringResource(R.string.saved_empty_subtitle),
                modifier = modifier
            )
        }

        shownItems.isEmpty() -> {
            SavedContent(
                modifier = modifier,
                selectedFilter = selectedFilter,
                onFilterSelected = onFilterSelected
            ) {
                SavedStateMessage(
                    title = savedEmptyFilterTitle(selectedFilter, searchQuery),
                    subtitle = savedEmptyFilterSubtitle(selectedFilter, searchQuery),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        else -> {
            SavedContent(
                modifier = modifier,
                selectedFilter = selectedFilter,
                onFilterSelected = onFilterSelected
            ) {
                WatchlistGrid(
                    shownItems = shownItems,
                    columnCount = columnCount,
                    isWatched = isWatched,
                    ratingBadge = ratingBadge,
                    onRemoveClick = onRemoveClick,
                    onItemClick = onItemClick
                )
            }
        }
    }
}

@Composable
private fun SavedContent(
    selectedFilter: SavedFilter,
    onFilterSelected: (SavedFilter) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        SavedFilterRow(
            selectedFilter = selectedFilter,
            onFilterSelected = onFilterSelected
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = SavedMediaScreenDefaults.GridOuterPadding)
        ) {
            content()
        }
    }
}

@Composable
private fun SavedFilterRow(
    selectedFilter: SavedFilter,
    onFilterSelected: (SavedFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = SavedMediaScreenDefaults.ScreenPadding,
                end = SavedMediaScreenDefaults.ScreenPadding,
                top = SavedMediaScreenDefaults.ScreenPadding,
                bottom = SavedMediaScreenDefaults.FilterRowBottomPadding
            ),
        horizontalArrangement = Arrangement.spacedBy(SavedMediaScreenDefaults.FilterChipSpacing)
    ) {
        SavedFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                ),
                label = {
                    Text(text = stringResource(filter.labelResId()))
                }
            )
        }
    }
}

@Composable
private fun SavedStateMessage(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(SavedMediaScreenDefaults.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = SavedMediaScreenDefaults.MessageSubtitleTopPadding)
            )
        }
    }
}

@Composable
private fun WatchlistGrid(
    shownItems: List<TmdbMediaItem>,
    columnCount: Int,
    isWatched: (Int) -> Boolean,
    ratingBadge: (Int) -> String?,
    onRemoveClick: (TmdbMediaItem) -> Unit,
    onItemClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columnCount),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SavedMediaScreenDefaults.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(SavedMediaScreenDefaults.GridSpacing),
        horizontalArrangement = Arrangement.spacedBy(SavedMediaScreenDefaults.GridSpacing)
    ) {
        items(shownItems, key = { it.id }) { item ->
            MediaItemCard(
                item = item,
                isWatched = isWatched(item.id),
                ratingBadge = ratingBadge(item.id),
                onRemoveClick = { onRemoveClick(item) },
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
    val FilterChipSpacing = 8.dp
    val FilterRowBottomPadding = 4.dp
    val ScreenPadding = 16.dp
    val MessageSubtitleTopPadding = 8.dp
}

private fun TmdbMediaItem.matchesSavedFilter(
    selectedFilter: SavedFilter,
    watchedIds: Set<Int>
): Boolean {
    return when (selectedFilter) {
        SavedFilter.ALL -> true
        SavedFilter.WATCHED -> watchedIds.contains(id)
        SavedFilter.UNWATCHED -> !watchedIds.contains(id)
    }
}

private fun SavedFilter.labelResId(): Int {
    return when (this) {
        SavedFilter.ALL -> R.string.saved_filter_all
        SavedFilter.WATCHED -> R.string.saved_filter_watched
        SavedFilter.UNWATCHED -> R.string.saved_filter_unwatched
    }
}

@Composable
private fun savedEmptyFilterTitle(
    selectedFilter: SavedFilter,
    searchQuery: String
): String {
    if (searchQuery.isNotBlank()) {
        return stringResource(R.string.saved_no_results_title)
    }

    return when (selectedFilter) {
        SavedFilter.ALL -> stringResource(R.string.saved_empty_title)
        SavedFilter.WATCHED -> stringResource(R.string.saved_empty_watched_title)
        SavedFilter.UNWATCHED -> stringResource(R.string.saved_empty_unwatched_title)
    }
}

@Composable
private fun savedEmptyFilterSubtitle(
    selectedFilter: SavedFilter,
    searchQuery: String
): String {
    if (searchQuery.isNotBlank()) {
        return stringResource(R.string.saved_no_results_subtitle)
    }

    return when (selectedFilter) {
        SavedFilter.ALL -> stringResource(R.string.saved_empty_subtitle)
        SavedFilter.WATCHED -> stringResource(R.string.saved_empty_watched_subtitle)
        SavedFilter.UNWATCHED -> stringResource(R.string.saved_empty_unwatched_subtitle)
    }
}

private fun List<TmdbMediaItem>.sortedForWatchlist(sortOption: WatchlistSortOption): List<TmdbMediaItem> {
    return when (sortOption) {
        WatchlistSortOption.NEWEST -> this
        WatchlistSortOption.OLDEST -> this.asReversed()
        WatchlistSortOption.TITLE_ASC -> sortedBy { (it.title ?: it.name ?: "").lowercase() }
        WatchlistSortOption.TITLE_DESC -> sortedByDescending {
            (it.title ?: it.name ?: "").lowercase()
        }
    }
}
