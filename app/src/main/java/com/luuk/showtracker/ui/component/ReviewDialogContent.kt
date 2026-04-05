package com.luuk.showtracker.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.luuk.showtracker.R
import com.luuk.showtracker.data.model.MediaReview
import com.luuk.showtracker.ui.theme.TextMuted

@Composable
fun ReviewDialogContent(
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
                .padding(horizontal = ReviewDialogDefaults.DialogOuterPadding),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(ReviewDialogDefaults.DialogInnerPadding)) {
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

                Spacer(modifier = Modifier.height(ReviewDialogDefaults.DialogSectionSpacing))

                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = { Text(stringResource(R.string.detail_review_text_label)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ReviewDialogDefaults.ReviewTextFieldHeight)
                )

                Spacer(modifier = Modifier.height(ReviewDialogDefaults.DialogSectionSpacing))

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

                Spacer(modifier = Modifier.height(ReviewDialogDefaults.RatingSectionSpacing))

                ReviewRatingSelector(
                    selectedRating = selectedRating,
                    onRatingSelected = { selectedRating = it }
                )

                Spacer(modifier = Modifier.height(ReviewDialogDefaults.DialogActionsTopSpacing))

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
private fun ReviewDialogHeader(title: String, onDismiss: () -> Unit) {
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
        horizontalArrangement = Arrangement.spacedBy(ReviewDialogDefaults.RatingStarSpacing)
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

private object ReviewDialogDefaults {
    val DialogOuterPadding = 20.dp
    val DialogInnerPadding = 24.dp
    val DialogSectionSpacing = 12.dp
    val RatingSectionSpacing = 16.dp
    val DialogActionsTopSpacing = 24.dp
    val ReviewTextFieldHeight = 140.dp
    val RatingStarSpacing = 4.dp
}
