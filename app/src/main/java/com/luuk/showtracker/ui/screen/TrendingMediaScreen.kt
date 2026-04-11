package com.luuk.showtracker.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.luuk.showtracker.R
import com.luuk.showtracker.data.model.TmdbMediaItem
import com.luuk.showtracker.ui.component.CompactPrimaryButton
import com.luuk.showtracker.ui.component.MediaItemCard
import com.luuk.showtracker.ui.viewmodel.MediaViewModel

@Composable
fun TrendingMediaScreen(
    viewModel: MediaViewModel,
    searchQuery: String,
    onItemClick: (TmdbMediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val mediaItems by viewModel.mediaItems.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val watchedIds by viewModel.watchedIds.collectAsState()
    val isTrendingLoading by viewModel.isTrendingLoading.collectAsState()
    val isSearchLoading by viewModel.isSearchLoading.collectAsState()
    val trendingErrorMessage by viewModel.trendingErrorMessage.collectAsState()
    val searchErrorMessage by viewModel.searchErrorMessage.collectAsState()
    val configuration = LocalConfiguration.current
    val columnCount = if (
        configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    ) {
        TrendingMediaScreenDefaults.LANDSCAPE_COLUMN_COUNT
    } else {
        TrendingMediaScreenDefaults.PORTRAIT_COLUMN_COUNT
    }
    val shownItems = if (searchQuery.isBlank()) mediaItems else searchResults

    LaunchedEffect(searchQuery) {
        viewModel.searchMedia(searchQuery)
    }

    TrendingMediaContent(
        mediaItems = mediaItems,
        shownItems = shownItems,
        isTrendingLoading = isTrendingLoading,
        isSearchLoading = isSearchLoading,
        trendingErrorMessage = trendingErrorMessage,
        searchErrorMessage = searchErrorMessage,
        searchQuery = searchQuery,
        columnCount = columnCount,
        modifier = modifier,
        isWatched = { itemId -> watchedIds.contains(itemId) },
        ratingBadge = { itemId -> reviews[itemId]?.rating?.toString() },
        onLoadNextPage = viewModel::loadNextPage,
        onRetryClick = viewModel::loadNextPage,
        onRefreshClick = viewModel::refreshTrending,
        onItemClick = onItemClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrendingMediaContent(
    mediaItems: List<TmdbMediaItem>,
    shownItems: List<TmdbMediaItem>,
    isTrendingLoading: Boolean,
    isSearchLoading: Boolean,
    trendingErrorMessage: Int?,
    searchErrorMessage: Int?,
    searchQuery: String,
    columnCount: Int,
    modifier: Modifier = Modifier,
    isWatched: (Int) -> Boolean,
    ratingBadge: (Int) -> String?,
    onLoadNextPage: () -> Unit,
    onRetryClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onItemClick: (TmdbMediaItem) -> Unit
) {
    PullToRefreshBox(
        isRefreshing = searchQuery.isBlank() && isTrendingLoading,
        onRefresh = {
            if (searchQuery.isBlank()) {
                onRefreshClick()
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = TrendingMediaScreenDefaults.GridOuterPadding)
        ) {
            if (searchQuery.isBlank()) {
                if (isTrendingLoading && mediaItems.isEmpty()) {
                    CenterLoadingIndicator()
                } else {
                    TrendingMediaGrid(
                        shownItems = shownItems,
                        isLoading = isTrendingLoading,
                        searchQuery = searchQuery,
                        columnCount = columnCount,
                        isWatched = isWatched,
                        ratingBadge = ratingBadge,
                        onLoadNextPage = onLoadNextPage,
                        onItemClick = onItemClick
                    )
                }

                if (trendingErrorMessage != null && mediaItems.isEmpty()) {
                    CenterErrorState(onRetryClick = onRetryClick)
                }
            } else {
                TrendingMediaGrid(
                    shownItems = shownItems,
                    isLoading = false,
                    searchQuery = searchQuery,
                    columnCount = columnCount,
                    isWatched = isWatched,
                    ratingBadge = ratingBadge,
                    onLoadNextPage = onLoadNextPage,
                    onItemClick = onItemClick
                )

                if (isSearchLoading && shownItems.isEmpty()) {
                    CenterLoadingIndicator()
                } else if (searchErrorMessage != null && shownItems.isEmpty()) {
                    CenterMessage(
                        text = stringResource(R.string.message_could_not_load_items),
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (!isSearchLoading && shownItems.isEmpty()) {
                    CenterMessage(
                        text = stringResource(R.string.message_no_results),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendingMediaGrid(
    shownItems: List<TmdbMediaItem>,
    isLoading: Boolean,
    searchQuery: String,
    columnCount: Int,
    isWatched: (Int) -> Boolean,
    ratingBadge: (Int) -> String?,
    onLoadNextPage: () -> Unit,
    onItemClick: (TmdbMediaItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columnCount),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(TrendingMediaScreenDefaults.GridContentPadding),
        verticalArrangement = Arrangement.spacedBy(TrendingMediaScreenDefaults.GridSpacing),
        horizontalArrangement = Arrangement.spacedBy(TrendingMediaScreenDefaults.GridSpacing)
    ) {
        itemsIndexed(shownItems) { index, item ->
            if (
                searchQuery.isBlank() &&
                index >= shownItems.size - TrendingMediaScreenDefaults.PREFETCH_THRESHOLD &&
                !isLoading
            ) {
                onLoadNextPage()
            }

            MediaItemCard(
                item = item,
                isWatched = isWatched(item.id),
                ratingBadge = ratingBadge(item.id),
                onClick = { onItemClick(item) }
            )
        }

        if (isLoading) {
            item {
                GridLoadingItem()
            }
        }
    }
}

@Composable
private fun BoxScope.CenterLoadingIndicator() {
    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
}

@Composable
private fun GridLoadingItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(TrendingMediaScreenDefaults.GridContentPadding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(TrendingMediaScreenDefaults.LoadingIndicatorSize)
        )
    }
}

@Composable
private fun BoxScope.CenterMessage(
    text: String,
    color: Color
) {
    Text(
        text = text,
        color = color,
        modifier = Modifier
            .align(Alignment.Center)
            .padding(TrendingMediaScreenDefaults.GridContentPadding)
    )
}

@Composable
private fun BoxScope.CenterErrorState(onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .padding(TrendingMediaScreenDefaults.GridContentPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.message_could_not_load_items),
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = stringResource(R.string.message_check_connection),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = TrendingMediaScreenDefaults.ErrorTextTopPadding)
        )
        CompactPrimaryButton(
            text = stringResource(R.string.action_try_again),
            onClick = onRetryClick,
            modifier = Modifier.padding(top = TrendingMediaScreenDefaults.ErrorButtonTopPadding)
        )
    }
}

private object TrendingMediaScreenDefaults {
    const val PORTRAIT_COLUMN_COUNT = 2
    const val LANDSCAPE_COLUMN_COUNT = 4
    const val PREFETCH_THRESHOLD = 5

    val GridOuterPadding = 4.dp
    val GridContentPadding = 16.dp
    val GridSpacing = 16.dp
    val LoadingIndicatorSize = 32.dp
    val ErrorTextTopPadding = 6.dp
    val ErrorButtonTopPadding = 12.dp
}
