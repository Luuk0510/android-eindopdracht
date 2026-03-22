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
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.luuk.showtracker.data.model.MediaReview
import com.luuk.showtracker.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    title: String,
    overview: String,
    posterPath: String?,
    genreNames: List<String>,
    isSaved: Boolean,
    isWatched: Boolean,
    currentReview: MediaReview?,
    onReviewSaved: (String, String, Int) -> Unit,
    onReviewDeleted: () -> Unit,
    onSaveClick: () -> Unit,
    onWatchedToggle: () -> Unit,
    onBackClick: () -> Unit
) {
    var showReviewDialog by remember { mutableStateOf(false) }
    var reviewTitle by remember(currentReview) { mutableStateOf(currentReview?.title ?: "") }
    var reviewText by remember(currentReview) { mutableStateOf(currentReview?.reviewText ?: "") }
    var selectedRating by remember(currentReview) { mutableIntStateOf(currentReview?.rating ?: 0) }

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
            currentReview = currentReview,
            isWatched = isWatched,
            onWriteReviewClick = { showReviewDialog = true },
            onWatchedToggle = onWatchedToggle
        )
    }

    if (showReviewDialog) {
        ReviewDialogContent(
            currentReview = currentReview,
            reviewTitle = reviewTitle,
            reviewText = reviewText,
            selectedRating = selectedRating,
            onReviewTitleChange = { reviewTitle = it },
            onReviewTextChange = { reviewText = it },
            onRatingSelected = { selectedRating = it },
            onDismiss = { showReviewDialog = false },
            onDelete = {
                onReviewDeleted()
                reviewTitle = ""
                reviewText = ""
                selectedRating = 0
                showReviewDialog = false
            },
            onSave = {
                if (reviewTitle.isNotBlank() && reviewText.isNotBlank() && selectedRating > 0) {
                    onReviewSaved(reviewTitle.trim(), reviewText.trim(), selectedRating)
                    showReviewDialog = false
                }
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
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w500$posterPath",
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
                contentDescription = "Back",
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
                contentDescription = if (isSaved) "Remove from watchlist" else "Save to watchlist",
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
    currentReview: MediaReview?,
    isWatched: Boolean,
    onWriteReviewClick: () -> Unit,
    onWatchedToggle: () -> Unit
) {
    Column(modifier = Modifier.padding(MediaDetailScreenDefaults.DetailHorizontalPadding)) {
        Text(
            text = "Overview",
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
            Button(
                onClick = onWriteReviewClick,
                modifier = Modifier.height(MediaDetailScreenDefaults.ActionButtonHeight),
                contentPadding = PaddingValues(
                    horizontal = MediaDetailScreenDefaults.ActionButtonHorizontalPadding,
                    vertical = MediaDetailScreenDefaults.ActionButtonVerticalPadding
                )
            ) {
                Text(
                    text = if (currentReview == null) "Write a review" else "Edit review",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            OutlinedButton(
                onClick = onWatchedToggle,
                modifier = Modifier.height(MediaDetailScreenDefaults.ActionButtonHeight),
                contentPadding = PaddingValues(
                    horizontal = MediaDetailScreenDefaults.ActionButtonHorizontalPadding,
                    vertical = MediaDetailScreenDefaults.ActionButtonVerticalPadding
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isWatched) {
                        MaterialTheme.colorScheme.secondary.copy(
                            alpha = MediaDetailScreenDefaults.WatchedButtonAlpha
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
                    text = if (isWatched) "Watched" else "Watch",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        if (currentReview != null) {
            Spacer(modifier = Modifier.height(MediaDetailScreenDefaults.SectionSpacing))
            ReviewCard(currentReview)
        }
    }
}

@Composable
private fun ReviewCard(review: MediaReview) {
    Surface(
        shape = RoundedCornerShape(MediaDetailScreenDefaults.ReviewCardCornerRadius),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(MediaDetailScreenDefaults.ReviewCardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${review.rating}/10",
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

            Spacer(modifier = Modifier.height(MediaDetailScreenDefaults.ReviewDateSpacing))

            Text(
                text = review.dateTime,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
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
private fun ReviewDialogContent(
    currentReview: MediaReview?,
    reviewTitle: String,
    reviewText: String,
    selectedRating: Int,
    onReviewTitleChange: (String) -> Unit,
    onReviewTextChange: (String) -> Unit,
    onRatingSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit
) {
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
                    title = if (currentReview == null) "Write a review" else "Edit review",
                    onDismiss = onDismiss
                )

                OutlinedTextField(
                    value = reviewTitle,
                    onValueChange = onReviewTitleChange,
                    label = { Text("Review title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(MediaDetailScreenDefaults.DialogSectionSpacing))

                OutlinedTextField(
                    value = reviewText,
                    onValueChange = onReviewTextChange,
                    label = { Text("Write your review") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MediaDetailScreenDefaults.ReviewTextFieldHeight)
                )

                Spacer(modifier = Modifier.height(MediaDetailScreenDefaults.DialogSectionSpacing))

                Text(
                    text = if (selectedRating > 0) "$selectedRating / 10 stars" else "Tap a star",
                    color = TextMuted,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(MediaDetailScreenDefaults.RatingSectionSpacing))

                ReviewRatingSelector(
                    selectedRating = selectedRating,
                    onRatingSelected = onRatingSelected
                )

                Spacer(modifier = Modifier.height(MediaDetailScreenDefaults.DialogActionsTopSpacing))

                if (currentReview == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = onSave,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Post review",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = onDelete) {
                            Text("Delete review")
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = onSave,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Update review",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
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
                contentDescription = "Close",
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
                    contentDescription = "Rate $rating",
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
    val GenrePillSpacing = 8.dp
    val GenrePillCornerRadius = 999.dp
    val GenrePillHorizontalPadding = 12.dp
    val GenrePillVerticalPadding = 6.dp
    val OverviewSpacing = 8.dp
    val SectionSpacing = 16.dp
    val ButtonSpacing = 10.dp
    val ActionButtonHeight = 38.dp
    val ActionButtonHorizontalPadding = 14.dp
    val ActionButtonVerticalPadding = 4.dp
    val WatchedIconSize = 18.dp
    val WatchedIconSpacing = 6.dp
    const val WatchedButtonAlpha = 0.18f
    val ReviewCardCornerRadius = 18.dp
    val ReviewCardPadding = 16.dp
    val ReviewRatingStarSize = 22.dp
    val ReviewDateSpacing = 6.dp
    val ReviewTextSpacing = 12.dp
    val DialogOuterPadding = 20.dp
    val DialogInnerPadding = 24.dp
    val DialogSectionSpacing = 12.dp
    val RatingSectionSpacing = 16.dp
    val DialogActionsTopSpacing = 24.dp
    val ReviewTextFieldHeight = 140.dp
    val RatingStarSpacing = 4.dp
}
