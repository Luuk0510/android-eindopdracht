package com.luuk.showtracker.ui.component

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun TmdbPosterImage(
    posterPath: String?,
    imageWidth: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val imageUrl = if (posterPath != null) {
        "$BASE_IMAGE_URL$imageWidth$posterPath"
    } else {
        null
    }
    val bitmapState = produceState<Bitmap?>(initialValue = null, imageUrl) {
        value = loadBitmap(imageUrl)
    }

    val bitmap = bitmapState.value
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {}
    }
}

private suspend fun loadBitmap(imageUrl: String?): Bitmap? {
    if (imageUrl.isNullOrBlank()) return null

    return withContext(Dispatchers.IO) {
        try {
            URL(imageUrl).openStream().use(BitmapFactory::decodeStream)
        } catch (_: Exception) {
            null
        }
    }
}

private const val BASE_IMAGE_URL = "https://image.tmdb.org/t/p/"
