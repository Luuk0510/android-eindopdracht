package com.luuk.showtracker.data.api

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.luuk.showtracker.data.model.TmdbMediaItem
import com.luuk.showtracker.data.model.TmdbResponse
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
    ): TmdbResponse {
        val url = "${TmdbServiceDefaults.BASEURL}trending/all/day?api_key=$apiKey&page=$page"
        return fetchResponse(url)
    }

    suspend fun searchMedia(
        apiKey: String,
        query: String
    ): TmdbResponse {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        val url = "${TmdbServiceDefaults.BASEURL}search/multi?api_key=$apiKey&query=$encodedQuery"
        return fetchResponse(url)
    }

    private suspend fun fetchResponse(url: String): TmdbResponse =
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                val request = JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    { response ->
                        continuation.resume(parseResponse(response))
                    },
                    { error ->
                        continuation.resumeWithException(error)
                    }
                )
                request.setShouldCache(true)
                continuation.invokeOnCancellation { request.cancel() }
                requestQueue.add(request)
            }
        }

    private fun parseResponse(response: JSONObject): TmdbResponse {
        val resultsArray = response.optJSONArray("results") ?: JSONArray()
        val items = mutableListOf<TmdbMediaItem>()

        for (index in 0 until resultsArray.length()) {
            val itemObject = resultsArray.optJSONObject(index) ?: continue
            items.add(parseMediaItem(itemObject))
        }

        return TmdbResponse(results = items)
    }

    private fun parseMediaItem(itemObject: JSONObject): TmdbMediaItem {
        return TmdbMediaItem(
            id = itemObject.optInt("id"),
            title = itemObject.optString("title").nullIfBlank(),
            name = itemObject.optString("name").nullIfBlank(),
            mediaType = itemObject.optString("media_type").nullIfBlank(),
            overview = itemObject.optString("overview"),
            genreIds = parseGenreIds(itemObject.optJSONArray("genre_ids")),
            posterPath = itemObject.optString("poster_path").nullIfBlank()
        )
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
