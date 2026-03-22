package com.luuk.showtracker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.luuk.showtracker.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    title: String,
    overview: String,
    posterPath: String?,
    isSaved: Boolean,
    currentRating: Int?,
    onRatingSelected: (Int) -> Unit,
    onRatingRemoved: () -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var showRatingDialog by remember { mutableStateOf(false) }
    var selectedRating by remember(currentRating) { mutableIntStateOf(currentRating ?: 0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp)
        ) {
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
                                Color(0x66000000),
                                Color.Transparent,
                                Color(0xCC121212)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = onSaveClick,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isSaved) "Remove from saved" else "Save movie",
                        tint = if (isSaved) MaterialTheme.colorScheme.secondary else Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = overview,
                style = MaterialTheme.typography.bodyLarge,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { showRatingDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (currentRating != null) {
                    Text(
                        text = "${currentRating}/10",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Give a rating",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }

    if (showRatingDialog) {
        Dialog(
            onDismissRequest = { showRatingDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Choose a rating",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { showRatingDialog = false }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (selectedRating > 0) "$selectedRating / 10 stars" else "Tap a star",
                        color = TextMuted,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val spacing = 4.dp
                        val starSize = (maxWidth - (spacing * 9)) / 10

                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (rating in 1..10) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Rate $rating",
                                    tint = if (rating <= selectedRating) {
                                        MaterialTheme.colorScheme.secondary
                                    } else {
                                        Color.Gray
                                    },
                                    modifier = Modifier
                                        .size(starSize)
                                        .clickable { selectedRating = rating }
                                )
                                if (rating < 10) {
                                    Spacer(modifier = Modifier.width(spacing))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (currentRating != null) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TextButton(
                                onClick = {
                                    onRatingRemoved()
                                    selectedRating = 0
                                    showRatingDialog = false
                                }
                            ) {
                                Text("Remove rating")
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Button(
                                onClick = {
                                    if (selectedRating > 0) {
                                        onRatingSelected(selectedRating)
                                    }
                                    showRatingDialog = false
                                },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Save",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                } else {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.weight(1f))
                            Button(
                                onClick = {
                                    if (selectedRating > 0) {
                                        onRatingSelected(selectedRating)
                                    }
                                    showRatingDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "Save",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
