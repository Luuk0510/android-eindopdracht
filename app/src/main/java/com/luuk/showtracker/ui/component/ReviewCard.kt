package com.luuk.showtracker.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.luuk.showtracker.R
import com.luuk.showtracker.data.model.MediaReview
import com.luuk.showtracker.ui.theme.TextMuted

@Composable
fun ReviewCard(
    review: MediaReview,
    profileName: String,
    profilePhotoUri: String?,
    onEditClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(ReviewCardDefaults.CornerRadius),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(ReviewCardDefaults.CardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileAvatar(
                    name = profileName,
                    photoUri = profilePhotoUri,
                    modifier = Modifier.size(ReviewCardDefaults.AvatarSize)
                )
                Spacer(modifier = Modifier.width(ReviewCardDefaults.HeaderSpacing))
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
                modifier = Modifier.padding(top = ReviewCardDefaults.TitleTopPadding)
            )

            Spacer(modifier = Modifier.padding(top = ReviewCardDefaults.TextSpacing))

            Text(
                text = review.reviewText,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )

            if (onEditClick != null) {
                TextButton(
                    onClick = onEditClick,
                    modifier = Modifier.padding(top = ReviewCardDefaults.EditButtonTopPadding)
                ) {
                    Text(stringResource(R.string.detail_edit_review))
                }
            }
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
        Spacer(modifier = Modifier.width(ReviewCardDefaults.RatingStarSpacing))
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(ReviewCardDefaults.RatingStarSize)
        )
    }
}

private object ReviewCardDefaults {
    val CornerRadius = 18.dp
    val CardPadding = 16.dp
    val AvatarSize = 42.dp
    val HeaderSpacing = 12.dp
    val TitleTopPadding = 12.dp
    val RatingStarSize = 22.dp
    val TextSpacing = 12.dp
    val RatingStarSpacing = 4.dp
    val EditButtonTopPadding = 12.dp
}
