package com.luuk.showtracker.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.luuk.showtracker.BuildConfig
import com.luuk.showtracker.data.api.RetrofitClient
import com.luuk.showtracker.data.model.TmdbMediaItem

@Composable
fun MediaListScreen(modifier: Modifier = Modifier) {
    // State to hold our list
    var mediaItems by remember { mutableStateOf(emptyList<TmdbMediaItem>()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch data when the screen opens
    LaunchedEffect(Unit) {
        try {
            // BuildConfig.TMDB_API_KEY comes from your secrets.properties
            val response = RetrofitClient.tmdbService.getTrending(BuildConfig.TMDB_API_KEY)
            mediaItems = response.results
        } catch (e: Exception) {
            // Handle errors here (e.g. log them)
        } finally {
            isLoading = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mediaItems) { item ->
                    MediaItemRow(item)
                }
            }
        }
    }
}

@Composable
fun MediaItemRow(item: TmdbMediaItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            // Coil Image Loading
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w200${item.posterPath}",
                contentDescription = null,
                modifier = Modifier.size(width = 100.dp, height = 150.dp)
            )

            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = item.title ?: item.name ?: "Unknown",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.overview,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}