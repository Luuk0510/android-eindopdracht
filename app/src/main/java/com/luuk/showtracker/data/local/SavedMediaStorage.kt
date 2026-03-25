package com.luuk.showtracker.data.local

import android.content.Context
import com.luuk.showtracker.data.model.TmdbMediaItem
import org.json.JSONArray
import org.json.JSONObject

class SavedMediaStorage(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences("saved_media_storage", Context.MODE_PRIVATE)

    fun loadSavedMedia(): List<TmdbMediaItem> {
        val json = sharedPreferences.getString(SavedMediaStorageDefaults.SavedMediaKey, null)
            ?: return emptyList()
        val savedMediaArray = JSONArray(json)
        val savedMedia = mutableListOf<TmdbMediaItem>()

        for (index in 0 until savedMediaArray.length()) {
            val itemObject = savedMediaArray.optJSONObject(index) ?: continue
            savedMedia.add(
                TmdbMediaItem(
                    id = itemObject.optInt(SavedMediaStorageDefaults.IdField),
                    title = itemObject.optString(SavedMediaStorageDefaults.TitleField).nullIfBlank(),
                    name = itemObject.optString(SavedMediaStorageDefaults.NameField).nullIfBlank(),
                    mediaType = itemObject
                        .optString(SavedMediaStorageDefaults.MediaTypeField)
                        .ifBlank { itemObject.optString(SavedMediaStorageDefaults.LegacyMediaTypeField) }
                        .nullIfBlank(),
                    overview = itemObject.optString(SavedMediaStorageDefaults.OverviewField),
                    genreIds = parseGenreIds(
                        itemObject.optJSONArray(SavedMediaStorageDefaults.GenreIdsField)
                            ?: itemObject.optJSONArray(SavedMediaStorageDefaults.LegacyGenreIdsField)
                    ),
                    releaseDate = itemObject.optString(SavedMediaStorageDefaults.ReleaseDateField).nullIfBlank(),
                    posterPath = itemObject
                        .optString(SavedMediaStorageDefaults.PosterPathField)
                        .ifBlank { itemObject.optString(SavedMediaStorageDefaults.LegacyPosterPathField) }
                        .nullIfBlank()
                )
            )
        }

        return savedMedia
    }

    fun saveSavedMedia(savedMedia: List<TmdbMediaItem>) {
        val savedMediaArray = JSONArray()

        savedMedia.forEach { item ->
            savedMediaArray.put(
                JSONObject().apply {
                    put(SavedMediaStorageDefaults.IdField, item.id)
                    put(SavedMediaStorageDefaults.TitleField, item.title)
                    put(SavedMediaStorageDefaults.NameField, item.name)
                    put(SavedMediaStorageDefaults.MediaTypeField, item.mediaType)
                    put(SavedMediaStorageDefaults.OverviewField, item.overview)
                    put(SavedMediaStorageDefaults.GenreIdsField, JSONArray(item.genreIds))
                    put(SavedMediaStorageDefaults.ReleaseDateField, item.releaseDate)
                    put(SavedMediaStorageDefaults.PosterPathField, item.posterPath)
                }
            )
        }

        sharedPreferences.edit()
            .putString(SavedMediaStorageDefaults.SavedMediaKey, savedMediaArray.toString())
            .apply()
    }
}

private fun parseGenreIds(genreArray: JSONArray?): List<Int> {
    if (genreArray == null) return emptyList()

    val genreIds = mutableListOf<Int>()
    for (index in 0 until genreArray.length()) {
        genreIds.add(genreArray.optInt(index))
    }
    return genreIds
}

private fun String.nullIfBlank(): String? {
    return takeUnless { it.isBlank() || it == "null" }
}

private object SavedMediaStorageDefaults {
    const val SavedMediaKey = "saved_media_json"
    const val IdField = "id"
    const val TitleField = "title"
    const val NameField = "name"
    const val MediaTypeField = "mediaType"
    const val OverviewField = "overview"
    const val GenreIdsField = "genreIds"
    const val ReleaseDateField = "releaseDate"
    const val PosterPathField = "posterPath"
    const val LegacyMediaTypeField = "media_type"
    const val LegacyGenreIdsField = "genre_ids"
    const val LegacyPosterPathField = "poster_path"
}
