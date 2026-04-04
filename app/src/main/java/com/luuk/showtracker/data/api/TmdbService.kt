package com.luuk.showtracker.data.api

import android.content.Context
import android.net.Uri
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.luuk.showtracker.data.model.TmdbMediaItem
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class TmdbService(context: Context) {
    private val requestQueue: RequestQueue = Volley.newRequestQueue(context.applicationContext)

    suspend fun getTrending(
        apiKey: String,
        page: Int
    ): List<TmdbMediaItem> {
        val url = "${TmdbServiceDefaults.BASE_URL}trending/all/day?api_key=$apiKey&page=$page"
        return fetchMediaItems(url)
    }

    suspend fun searchMedia(
        apiKey: String,
        query: String
    ): List<TmdbMediaItem> {
        val encodedQuery = Uri.encode(query)
        val url = "${TmdbServiceDefaults.BASE_URL}search/multi?api_key=$apiKey&query=$encodedQuery"
        return fetchMediaItems(url)
    }

    private suspend fun fetchMediaItems(url: String): List<TmdbMediaItem> {
        return suspendCoroutine { continuation ->
            val request = JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                { response -> continuation.resume(parseMediaItems(response)) },
                { error -> continuation.resumeWithException(error) }
            )
            request.setShouldCache(true)
            requestQueue.add(request)
        }
    }

    private fun parseMediaItems(response: JSONObject): List<TmdbMediaItem> {
        val resultsArray = response.optJSONArray("results") ?: JSONArray()
        val items = mutableListOf<TmdbMediaItem>()

        for (index in 0 until resultsArray.length()) {
            val itemJson = resultsArray.optJSONObject(index) ?: continue

            val title = itemJson.optString("title").nullIfBlank()
            val name = itemJson.optString("name").nullIfBlank()
            val mediaType = itemJson.optString("media_type").nullIfBlank()
            val overview = itemJson.optString("overview")
            val releaseDateText = itemJson.optString("release_date")
            val firstAirDateText = itemJson.optString("first_air_date")
            val releaseDate = releaseDateText.ifBlank { firstAirDateText }
            val posterPath = itemJson.optString("poster_path").nullIfBlank()

            items.add(
                TmdbMediaItem(
                    id = itemJson.optInt("id"),
                    title = title,
                    name = name,
                    mediaType = mediaType,
                    overview = overview,
                    genreIds = parseGenreIds(itemJson.optJSONArray("genre_ids")),
                    releaseDate = releaseDate.nullIfBlank(),
                    posterPath = posterPath
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
    if (isBlank() || this == "null") {
        return null
    }

    return this
}

private object TmdbServiceDefaults {
    const val BASE_URL = "https://api.themoviedb.org/3/"
}
