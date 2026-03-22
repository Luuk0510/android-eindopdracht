package com.luuk.showtracker.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import coil.compose.AsyncImage
import com.luuk.showtracker.data.model.TmdbMediaItem
import com.luuk.showtracker.ui.theme.SurfaceDark
import com.luuk.showtracker.ui.theme.TextMuted
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
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val configuration = LocalConfiguration.current
    val columnCount = if (
        configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    ) {
        3
    } else {
        2
    }
    val shownItems = if (searchQuery.isBlank()) mediaItems else searchResults

    LaunchedEffect(searchQuery) {
        viewModel.searchMedia(searchQuery)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp)
    ) {
        if (isLoading && mediaItems.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnCount),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(shownItems) { index, item ->
                    if (searchQuery.isBlank() && index >= shownItems.size - 5 && !isLoading) {
                        viewModel.loadNextPage()
                    }

                    MediaItemRow(
                        item = item,
                        onClick = { onItemClick(item) }
                    )
                }

                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }
        }
        
        if (errorMessage != null && mediaItems.isEmpty()) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            )
        }

        if (!isLoading && shownItems.isEmpty() && searchQuery.isNotBlank()) {
            Text(
                text = "No results found.",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun MediaItemRow(item: TmdbMediaItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w200${item.posterPath}",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.68f)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = item.title ?: item.name ?: "Unknown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = Color.White,
            modifier = Modifier.padding(top = 10.dp)
        )

        Text(
            text = if (item.title != null) "MOVIE" else "TV SHOW",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
