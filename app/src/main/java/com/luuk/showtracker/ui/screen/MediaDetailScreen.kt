package com.luuk.showtracker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.luuk.showtracker.R
import com.luuk.showtracker.data.model.MediaReview
import com.luuk.showtracker.ui.component.CompactPrimaryButton
import com.luuk.showtracker.ui.component.CompactPrimaryButtonDefaults
import com.luuk.showtracker.ui.component.ProfileAvatar
import com.luuk.showtracker.ui.component.TmdbPosterImage
import com.luuk.showtracker.ui.theme.TextMuted
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
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
                        GenreChip(genreName)
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
private fun GenreChip(genreName: String) {
    Text(
        text = genreName,
        color = MaterialTheme.colorScheme.onSecondary,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .padding(
                end = MediaDetailScreenDefaults.GenrePillSpacing,
                bottom = MediaDetailScreenDefaults.GenreSpacing
            )
            .background(
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(MediaDetailScreenDefaults.GenrePillCornerRadius)
            )
            .padding(
                horizontal = MediaDetailScreenDefaults.GenrePillHorizontalPadding,
                vertical = MediaDetailScreenDefaults.GenrePillVerticalPadding
            )
    )
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

@Composable
private fun ReviewCard(
    review: MediaReview,
    profileName: String,
    profilePhotoUri: String?
) {
    Surface(
        shape = RoundedCornerShape(MediaDetailScreenDefaults.ReviewCardCornerRadius),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(MediaDetailScreenDefaults.ReviewCardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileAvatar(
                    name = profileName,
                    photoUri = profilePhotoUri,
                    modifier = Modifier.size(MediaDetailScreenDefaults.ReviewAvatarSize)
                )
                Spacer(modifier = Modifier.width(MediaDetailScreenDefaults.ReviewHeaderSpacing))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profileName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = review.dateTime,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
                ReviewRatingText(rating = review.rating)
            }

            Text(
                text = review.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(top = MediaDetailScreenDefaults.ReviewTitleTopPadding)
            )

            Spacer(modifier = Modifier.height(MediaDetailScreenDefaults.ReviewTextSpacing))

            Text(
                text = review.reviewText,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ReviewRatingText(rating: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.detail_rating_value, rating),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.width(MediaDetailScreenDefaults.RatingStarSpacing))
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(MediaDetailScreenDefaults.ReviewRatingStarSize)
        )
    }
}

@Composable
private fun ReviewDialogContent(
    currentReview: MediaReview?,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onSave: (String, String, Int) -> Unit
) {
    var reviewTitle by remember(currentReview) { mutableStateOf(currentReview?.title.orEmpty()) }
    var reviewText by remember(currentReview) { mutableStateOf(currentReview?.reviewText.orEmpty()) }
    var selectedRating by remember(currentReview) { mutableIntStateOf(currentReview?.rating ?: 0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MediaDetailScreenDefaults.DialogOuterPadding),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(MediaDetailScreenDefaults.DialogInnerPadding)) {
                ReviewDialogHeader(
                    title = stringResource(
                        if (currentReview == null) R.string.detail_write_review else R.string.detail_edit_review
                    ),
                    onDismiss = onDismiss
                )

                OutlinedTextField(
                    value = reviewTitle,
                    onValueChange = { reviewTitle = it },
                    label = { Text(stringResource(R.string.detail_review_title_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(MediaDetailScreenDefaults.DialogSectionSpacing))

                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = { Text(stringResource(R.string.detail_review_text_label)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MediaDetailScreenDefaults.ReviewTextFieldHeight)
                )

                Spacer(modifier = Modifier.height(MediaDetailScreenDefaults.DialogSectionSpacing))

                Text(
                    text = if (selectedRating > 0) {
                        stringResource(R.string.detail_rating_stars, selectedRating)
                    } else {
                        stringResource(R.string.detail_tap_star)
                    },
                    color = TextMuted,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(MediaDetailScreenDefaults.RatingSectionSpacing))

                ReviewRatingSelector(
                    selectedRating = selectedRating,
                    onRatingSelected = { selectedRating = it }
                )

                Spacer(modifier = Modifier.height(MediaDetailScreenDefaults.DialogActionsTopSpacing))

                if (currentReview == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CompactPrimaryButton(
                            text = stringResource(R.string.detail_post_review),
                            onClick = {
                                if (reviewTitle.isNotBlank() && reviewText.isNotBlank() && selectedRating > 0) {
                                    onSave(reviewTitle.trim(), reviewText.trim(), selectedRating)
                                }
                            }
                        )
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = onDelete) {
                            Text(stringResource(R.string.detail_delete_review))
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        CompactPrimaryButton(
                            text = stringResource(R.string.detail_update_review),
                            onClick = {
                                if (reviewTitle.isNotBlank() && reviewText.isNotBlank() && selectedRating > 0) {
                                    onSave(reviewTitle.trim(), reviewText.trim(), selectedRating)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewDialogHeader(
    title: String,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.content_close),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun ReviewRatingSelector(
    selectedRating: Int,
    onRatingSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MediaDetailScreenDefaults.RatingStarSpacing)
    ) {
        for (rating in 1..10) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clickable { onRatingSelected(rating) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = stringResource(R.string.content_rate_star, rating),
                    tint = if (rating <= selectedRating) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        Color.Gray
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
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
    val GenrePillCornerRadius = 999.dp
    val GenrePillHorizontalPadding = 12.dp
    val GenrePillVerticalPadding = 6.dp
    val OverviewSpacing = 8.dp
    val SectionSpacing = 16.dp
    val ButtonSpacing = 10.dp
    val WatchedIconSize = 18.dp
    val WatchedIconSpacing = 6.dp
    const val WATCHED_BUTTON_ALPHA = 0.18f
    val ReviewCardCornerRadius = 18.dp
    val ReviewCardPadding = 16.dp
    val ReviewAvatarSize = 42.dp
    val ReviewHeaderSpacing = 12.dp
    val ReviewTitleTopPadding = 12.dp
    val ReviewRatingStarSize = 22.dp
    val ReviewTextSpacing = 12.dp
    val DialogOuterPadding = 20.dp
    val DialogInnerPadding = 24.dp
    val DialogSectionSpacing = 12.dp
    val RatingSectionSpacing = 16.dp
    val DialogActionsTopSpacing = 24.dp
    val ReviewTextFieldHeight = 140.dp
    val RatingStarSpacing = 4.dp
    const val POSTER_IMAGE_WIDTH = "w500"
}

private fun String.toDisplayDate(): String {
    return runCatching {
        LocalDate.parse(this).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }.getOrDefault(this)
}
