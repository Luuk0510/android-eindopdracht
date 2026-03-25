package com.luuk.showtracker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luuk.showtracker.R
import com.luuk.showtracker.data.model.MediaReview
import com.luuk.showtracker.ui.component.CompactPrimaryButton
import com.luuk.showtracker.ui.component.CompactPrimaryButtonDefaults
import com.luuk.showtracker.ui.component.GenrePill
import com.luuk.showtracker.ui.component.ReviewCard
import com.luuk.showtracker.ui.component.ReviewDialogContent
import com.luuk.showtracker.ui.component.TmdbPosterImage
import com.luuk.showtracker.ui.theme.TextMuted
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun MediaDetailScreen(
    title: String,
    overview: String,
    posterPath: String?,
    genreNames: List<String>,
    releaseDate: String?,
    isSaved: Boolean,
    isWatched: Boolean,
    profileName: String,
    profilePhotoUri: String?,
    currentReview: MediaReview?,
    onReviewSaved: (String, String, Int) -> Unit,
    onReviewDeleted: () -> Unit,
    onSaveClick: () -> Unit,
    onWatchedToggle: () -> Unit,
    onBackClick: () -> Unit
) {
    val showReviewDialog = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        DetailHeroSection(
            title = title,
            posterPath = posterPath,
            genreNames = genreNames,
            isSaved = isSaved,
            onSaveClick = onSaveClick,
            onBackClick = onBackClick
        )

        DetailContentSection(
            overview = overview,
            releaseDate = releaseDate,
            profileName = profileName,
            profilePhotoUri = profilePhotoUri,
            currentReview = currentReview,
            isWatched = isWatched,
            onWriteReviewClick = { showReviewDialog.value = true },
            onWatchedToggle = onWatchedToggle
        )
    }

    if (showReviewDialog.value) {
        ReviewDialogContent(
            currentReview = currentReview,
            onDismiss = { showReviewDialog.value = false },
            onDelete = {
                onReviewDeleted()
                showReviewDialog.value = false
            },
            onSave = { reviewTitle, reviewText, selectedRating ->
                onReviewSaved(reviewTitle, reviewText, selectedRating)
                showReviewDialog.value = false
            }
        )
    }
}

@Composable
private fun DetailHeroSection(
    title: String,
    posterPath: String?,
    genreNames: List<String>,
    isSaved: Boolean,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(MediaDetailScreenDefaults.PosterHeight)
    ) {
        DetailPosterBackground(posterPath = posterPath)
        DetailTopActions(
            isSaved = isSaved,
            onSaveClick = onSaveClick,
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = MediaDetailScreenDefaults.DetailHorizontalPadding,
                    end = MediaDetailScreenDefaults.DetailHorizontalPadding,
                    top = MediaDetailScreenDefaults.TitleBlockTopPadding,
                    bottom = MediaDetailScreenDefaults.TitleBlockBottomPadding
                )
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            if (genreNames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(MediaDetailScreenDefaults.GenreSpacing))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    genreNames.forEach { genreName ->
                        GenrePill(
                            genreName = genreName,
                            modifier = Modifier.padding(
                                end = MediaDetailScreenDefaults.GenrePillSpacing,
                                bottom = MediaDetailScreenDefaults.GenreSpacing
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailPosterBackground(posterPath: String?) {
    if (!posterPath.isNullOrBlank()) {
        TmdbPosterImage(
            posterPath = posterPath,
            imageWidth = MediaDetailScreenDefaults.POSTER_IMAGE_WIDTH,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    } else {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {}
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MediaDetailScreenDefaults.PosterOverlayTopColor,
                        Color.Transparent,
                        MediaDetailScreenDefaults.PosterOverlayBottomColor
                    )
                )
            )
    )
}

@Composable
private fun DetailTopActions(
    isSaved: Boolean,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                horizontal = MediaDetailScreenDefaults.TopActionsHorizontalPadding,
                vertical = MediaDetailScreenDefaults.TopActionsVerticalPadding
            )
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(MediaDetailScreenDefaults.TopIconButtonSize)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.content_back),
                tint = Color.White,
                modifier = Modifier.size(MediaDetailScreenDefaults.TopIconSize)
            )
        }

        IconButton(
            onClick = onSaveClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(MediaDetailScreenDefaults.TopIconButtonSize)
        ) {
            Icon(
                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = stringResource(
                    if (isSaved) {
                        R.string.content_remove_from_watchlist
                    } else {
                        R.string.content_save_to_watchlist
                    }
                ),
                tint = if (isSaved) MaterialTheme.colorScheme.secondary else Color.White,
                modifier = Modifier.size(MediaDetailScreenDefaults.TopIconSize)
            )
        }
    }
}

@Composable
private fun DetailContentSection(
    overview: String,
    releaseDate: String?,
    profileName: String,
    profilePhotoUri: String?,
    currentReview: MediaReview?,
    isWatched: Boolean,
    onWriteReviewClick: () -> Unit,
    onWatchedToggle: () -> Unit
) {
    Column(modifier = Modifier.padding(MediaDetailScreenDefaults.DetailHorizontalPadding)) {
        if (!releaseDate.isNullOrBlank()) {
            Text(
                text = stringResource(R.string.detail_release_date, releaseDate.toDisplayDate()),
                style = MaterialTheme.typography.bodyLarge,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(MediaDetailScreenDefaults.ReleaseDateBottomSpacing))
        }

        Text(
            text = stringResource(R.string.detail_overview),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(MediaDetailScreenDefaults.OverviewSpacing))
        Text(
            text = overview,
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(MediaDetailScreenDefaults.SectionSpacing))

        Row(
            horizontalArrangement = Arrangement.spacedBy(MediaDetailScreenDefaults.ButtonSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompactPrimaryButton(
                text = stringResource(
                    if (currentReview == null) R.string.detail_write_review else R.string.detail_edit_review
                ),
                onClick = onWriteReviewClick
            )

            OutlinedButton(
                onClick = onWatchedToggle,
                modifier = Modifier.height(CompactPrimaryButtonDefaults.Height),
                contentPadding = PaddingValues(
                    horizontal = CompactPrimaryButtonDefaults.HorizontalPadding,
                    vertical = CompactPrimaryButtonDefaults.VerticalPadding
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isWatched) {
                        MaterialTheme.colorScheme.secondary.copy(
                            alpha = MediaDetailScreenDefaults.WATCHED_BUTTON_ALPHA
                        )
                    } else {
                        Color.Transparent
                    },
                    contentColor = if (isWatched) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        Color.White
                    }
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(MediaDetailScreenDefaults.WatchedIconSize)
                )
                Spacer(modifier = Modifier.width(MediaDetailScreenDefaults.WatchedIconSpacing))
                Text(
                    text = stringResource(
                        if (isWatched) R.string.detail_watched else R.string.detail_watch
                    ),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        if (currentReview != null) {
            Spacer(modifier = Modifier.height(MediaDetailScreenDefaults.SectionSpacing))
            ReviewCard(
                review = currentReview,
                profileName = profileName,
                profilePhotoUri = profilePhotoUri
            )
        }
    }
}

private object MediaDetailScreenDefaults {
    val PosterHeight = 520.dp
    val PosterOverlayTopColor = Color(0x66000000)
    val PosterOverlayBottomColor = Color(0xCC121212)
    val TopActionsHorizontalPadding = 8.dp
    val TopActionsVerticalPadding = 12.dp
    val TopIconButtonSize = 52.dp
    val TopIconSize = 30.dp
    val DetailHorizontalPadding = 20.dp
    val TitleBlockTopPadding = 24.dp
    val TitleBlockBottomPadding = 8.dp
    val GenreSpacing = 2.dp
    val ReleaseDateBottomSpacing = 12.dp
    val GenrePillSpacing = 8.dp
    val OverviewSpacing = 8.dp
    val SectionSpacing = 16.dp
    val ButtonSpacing = 10.dp
    val WatchedIconSize = 18.dp
    val WatchedIconSpacing = 6.dp
    const val WATCHED_BUTTON_ALPHA = 0.18f
    const val POSTER_IMAGE_WIDTH = "w500"
}

private fun String.toDisplayDate(): String {
    return runCatching {
        LocalDate.parse(this).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    }.getOrDefault(this)
}
