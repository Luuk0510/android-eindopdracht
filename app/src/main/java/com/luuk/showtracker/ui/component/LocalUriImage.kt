package com.luuk.showtracker.ui.component

import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LocalUriImage(
    imageUri: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val bitmapState = produceState<android.graphics.Bitmap?>(initialValue = null, imageUri) {
        value = loadBitmap(context, imageUri)
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

private suspend fun loadBitmap(
    context: android.content.Context,
    imageUri: String?
): android.graphics.Bitmap? {
    if (imageUri.isNullOrBlank()) return null

    return withContext(Dispatchers.IO) {
        try {
            val parsedUri = imageUri.toUri()
            if (parsedUri.scheme == "file") {
                BitmapFactory.decodeFile(parsedUri.path)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, parsedUri)
                ImageDecoder.decodeBitmap(source)
            }
        } catch (_: Exception) {
            null
        }
    }
}
