package com.luuk.showtracker.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import com.luuk.showtracker.data.model.TmdbMediaItem
import com.luuk.showtracker.ui.component.TmdbPosterImage
import com.luuk.showtracker.ui.theme.SurfaceDark
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
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val configuration = LocalConfiguration.current
    val columnCount = if (
        configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    ) {
        TrendingMediaScreenDefaults.LandscapeColumnCount
    } else {
        TrendingMediaScreenDefaults.PortraitColumnCount
    }
    val shownItems = if (searchQuery.isBlank()) mediaItems else searchResults

    LaunchedEffect(searchQuery) {
        viewModel.searchMedia(searchQuery)
    }

    TrendingMediaContent(
        mediaItems = mediaItems,
        shownItems = shownItems,
        isLoading = isLoading,
        errorMessage = errorMessage,
        searchQuery = searchQuery,
        columnCount = columnCount,
        modifier = modifier,
        isWatched = { itemId -> watchedIds.contains(itemId) },
        ratingBadge = { itemId -> reviews[itemId]?.rating?.toString() },
        onLoadNextPage = viewModel::loadNextPage,
        onItemClick = onItemClick
    )
}

@Composable
private fun TrendingMediaContent(
    mediaItems: List<TmdbMediaItem>,
    shownItems: List<TmdbMediaItem>,
    isLoading: Boolean,
    errorMessage: String?,
    searchQuery: String,
    columnCount: Int,
    modifier: Modifier = Modifier,
    isWatched: (Int) -> Boolean,
    ratingBadge: (Int) -> String?,
    onLoadNextPage: () -> Unit,
    onItemClick: (TmdbMediaItem) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = TrendingMediaScreenDefaults.GridOuterPadding)
    ) {
        if (isLoading && mediaItems.isEmpty()) {
            CenterLoadingIndicator()
        } else {
            TrendingMediaGrid(
                shownItems = shownItems,
                isLoading = isLoading,
                searchQuery = searchQuery,
                columnCount = columnCount,
                isWatched = isWatched,
                ratingBadge = ratingBadge,
                onLoadNextPage = onLoadNextPage,
                onItemClick = onItemClick
            )
        }

        if (errorMessage != null && mediaItems.isEmpty()) {
            CenterMessage(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (!isLoading && shownItems.isEmpty() && searchQuery.isNotBlank()) {
            CenterMessage(
                text = "No results found.",
                color = MaterialTheme.colorScheme.onBackground
            )
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
                index >= shownItems.size - TrendingMediaScreenDefaults.PrefetchThreshold &&
                !isLoading
            ) {
                onLoadNextPage()
            }

            MediaItemRow(
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
fun MediaItemRow(
    item: TmdbMediaItem,
    isWatched: Boolean = false,
    ratingBadge: String? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Card(
            shape = RoundedCornerShape(TrendingMediaScreenDefaults.MediaCardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            elevation = CardDefaults.cardElevation(
                defaultElevation = TrendingMediaScreenDefaults.MediaCardElevation
            )
        ) {
            Box {
                TmdbPosterImage(
                    posterPath = item.posterPath,
                    imageWidth = TrendingMediaScreenDefaults.PosterImageWidth,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(TrendingMediaScreenDefaults.MediaPosterAspectRatio)
                        .clip(RoundedCornerShape(TrendingMediaScreenDefaults.MediaCardCornerRadius)),
                    contentScale = ContentScale.Crop
                )

                if (isWatched) {
                    Surface(
                        color = TrendingMediaScreenDefaults.MediaOverlayColor,
                        shape = RoundedCornerShape(
                            bottomStart = TrendingMediaScreenDefaults.MediaBadgeCornerRadius
                        ),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Visibility,
                            contentDescription = "Watched",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(
                                horizontal = TrendingMediaScreenDefaults.WatchedBadgeHorizontalPadding,
                                vertical = TrendingMediaScreenDefaults.WatchedBadgeVerticalPadding
                            )
                        )
                    }
                }

                if (!ratingBadge.isNullOrBlank()) {
                    Surface(
                        color = TrendingMediaScreenDefaults.MediaOverlayColor,
                        shape = RoundedCornerShape(
                            topStart = TrendingMediaScreenDefaults.MediaBadgeCornerRadius
                        ),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(
                                horizontal = TrendingMediaScreenDefaults.RatingBadgeHorizontalPadding,
                                vertical = TrendingMediaScreenDefaults.RatingBadgeVerticalPadding
                            )
                        ) {
                            Text(
                                text = ratingBadge,
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .padding(start = TrendingMediaScreenDefaults.RatingStarSpacing)
                                    .size(TrendingMediaScreenDefaults.RatingStarSize)
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = item.title ?: item.name ?: "Unknown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = Color.White,
            modifier = Modifier.padding(top = TrendingMediaScreenDefaults.TitleTopPadding)
        )
    }
}

private object TrendingMediaScreenDefaults {
    const val PortraitColumnCount = 2
    const val LandscapeColumnCount = 3
    const val PrefetchThreshold = 5

    val GridOuterPadding = 4.dp
    val GridContentPadding = 16.dp
    val GridSpacing = 16.dp
    val LoadingIndicatorSize = 32.dp
    val MediaCardCornerRadius = 16.dp
    val MediaCardElevation = 6.dp
    const val MediaPosterAspectRatio = 0.68f
    val MediaBadgeCornerRadius = 12.dp
    val WatchedBadgeHorizontalPadding = 10.dp
    val WatchedBadgeVerticalPadding = 8.dp
    val RatingBadgeHorizontalPadding = 10.dp
    val RatingBadgeVerticalPadding = 6.dp
    val RatingStarSpacing = 4.dp
    val RatingStarSize = 16.dp
    val TitleTopPadding = 10.dp
    val MediaOverlayColor = Color(0xCC121212)
    const val PosterImageWidth = "w200"
}
