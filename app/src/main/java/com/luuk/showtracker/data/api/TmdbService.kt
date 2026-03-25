package com.luuk.showtracker.data.api

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.luuk.showtracker.data.model.TmdbMediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TmdbService(context: Context) {
    private val requestQueue: RequestQueue = Volley.newRequestQueue(context.applicationContext)

    suspend fun getTrending(
        apiKey: String,
        page: Int
    ): List<TmdbMediaItem> {
        val url = "${TmdbServiceDefaults.BASEURL}trending/all/day?api_key=$apiKey&page=$page"
        return fetchMediaItems(url)
    }

    suspend fun searchMedia(
        apiKey: String,
        query: String
    ): List<TmdbMediaItem> {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        val url = "${TmdbServiceDefaults.BASEURL}search/multi?api_key=$apiKey&query=$encodedQuery"
        return fetchMediaItems(url)
    }

    private suspend fun fetchMediaItems(url: String): List<TmdbMediaItem> =
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                val request = JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    { response -> continuation.resume(parseMediaItems(response)) },
                    { error -> continuation.resumeWithException(error) }
                )
                request.setShouldCache(true)
                continuation.invokeOnCancellation { request.cancel() }
                requestQueue.add(request)
            }
        }

    private fun parseMediaItems(response: JSONObject): List<TmdbMediaItem> {
        val resultsArray = response.optJSONArray("results") ?: JSONArray()
        val items = mutableListOf<TmdbMediaItem>()

        for (index in 0 until resultsArray.length()) {
            val itemJson = resultsArray.optJSONObject(index) ?: continue
            items.add(
                TmdbMediaItem(
                    id = itemJson.optInt("id"),
                    title = itemJson.optString("title").nullIfBlank(),
                    name = itemJson.optString("name").nullIfBlank(),
                    mediaType = itemJson.optString("media_type").nullIfBlank(),
                    overview = itemJson.optString("overview"),
                    genreIds = parseGenreIds(itemJson.optJSONArray("genre_ids")),
                    releaseDate = itemJson.optString("release_date")
                        .ifBlank { itemJson.optString("first_air_date") }
                        .nullIfBlank(),
                    posterPath = itemJson.optString("poster_path").nullIfBlank()
                )
            )
        }

        return items
    }

    private fun parseGenreIds(genreArray: JSONArray?): List<Int> {
        if (genreArray == null) return emptyList()

        val genreIds = mutableListOf<Int>()
        for (index in 0 until genreArray.length()) {
            genreIds.add(genreArray.optInt(index))
        }
        return genreIds
    }
}

private fun String.nullIfBlank(): String? {
    return takeUnless { it.isBlank() || it == "null" }
}

private object TmdbServiceDefaults {
    const val BASEURL = "https://api.themoviedb.org/3/"
}
