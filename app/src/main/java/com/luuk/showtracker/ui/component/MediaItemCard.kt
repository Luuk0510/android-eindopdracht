package com.luuk.showtracker.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.luuk.showtracker.R
import com.luuk.showtracker.data.model.TmdbMediaItem
import com.luuk.showtracker.ui.theme.SurfaceDark

@Composable
fun MediaItemCard(
    item: TmdbMediaItem,
    isWatched: Boolean = false,
    ratingBadge: String? = null,
    onRemoveClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Card(
            shape = RoundedCornerShape(MediaItemCardDefaults.CardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            elevation = CardDefaults.cardElevation(defaultElevation = MediaItemCardDefaults.CardElevation)
        ) {
            Box {
                TmdbPosterImage(
                    posterPath = item.posterPath,
                    imageWidth = MediaItemCardDefaults.POSTER_IMAGE_WIDTH,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(MediaItemCardDefaults.POSTER_ASPECT_RATIO)
                        .clip(RoundedCornerShape(MediaItemCardDefaults.CardCornerRadius)),
                    contentScale = ContentScale.Crop
                )

                if (isWatched) {
                    Surface(
                        color = MediaItemCardDefaults.OverlayColor,
                        shape = RoundedCornerShape(bottomStart = MediaItemCardDefaults.BadgeCornerRadius),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Visibility,
                            contentDescription = stringResource(R.string.content_watched),
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(
                                horizontal = MediaItemCardDefaults.BadgeHorizontalPadding,
                                vertical = MediaItemCardDefaults.BadgeVerticalPadding
                            )
                        )
                    }
                }

                if (onRemoveClick != null) {
                    Surface(
                        color = MediaItemCardDefaults.OverlayColor,
                        shape = RoundedCornerShape(bottomEnd = MediaItemCardDefaults.BadgeCornerRadius),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        IconButton(onClick = onRemoveClick) {
                            Icon(
                                imageVector = Icons.Filled.BookmarkRemove,
                                contentDescription = stringResource(R.string.content_remove_from_watchlist),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                if (!ratingBadge.isNullOrBlank()) {
                    Surface(
                        color = MediaItemCardDefaults.OverlayColor,
                        shape = RoundedCornerShape(topStart = MediaItemCardDefaults.BadgeCornerRadius),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(
                                horizontal = MediaItemCardDefaults.RatingBadgeHorizontalPadding,
                                vertical = MediaItemCardDefaults.RatingBadgeVerticalPadding
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
                                    .padding(start = MediaItemCardDefaults.RatingStarSpacing)
                                    .size(MediaItemCardDefaults.RatingStarSize)
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = item.title ?: item.name ?: stringResource(R.string.message_unknown),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = Color.White,
            modifier = Modifier.padding(top = MediaItemCardDefaults.TitleTopPadding)
        )
    }
}

private object MediaItemCardDefaults {
    val CardCornerRadius = 16.dp
    val CardElevation = 6.dp
    val BadgeCornerRadius = 12.dp
    val BadgeHorizontalPadding = 10.dp
    val BadgeVerticalPadding = 8.dp
    val RatingBadgeHorizontalPadding = 10.dp
    val RatingBadgeVerticalPadding = 6.dp
    val RatingStarSpacing = 4.dp
    val RatingStarSize = 16.dp
    val TitleTopPadding = 10.dp
    val OverlayColor = Color(0xCC121212)

    const val POSTER_ASPECT_RATIO = 0.68f
    const val POSTER_IMAGE_WIDTH = "w200"
}
