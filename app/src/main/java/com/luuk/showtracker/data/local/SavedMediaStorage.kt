package com.luuk.showtracker.data.local

import android.content.Context
import androidx.core.content.edit
import com.luuk.showtracker.data.model.TmdbMediaItem
import org.json.JSONArray
import org.json.JSONObject

class SavedMediaStorage(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences("saved_media_storage", Context.MODE_PRIVATE)

    fun loadSavedMedia(): List<TmdbMediaItem> {
        val json = sharedPreferences.getString(SavedMediaStorageDefaults.SAVED_MEDIA_KEY, null)
            ?: return emptyList()
        val savedMediaArray = JSONArray(json)
        val savedMedia = mutableListOf<TmdbMediaItem>()

        for (index in 0 until savedMediaArray.length()) {
            val itemObject = savedMediaArray.optJSONObject(index) ?: continue
            var mediaType = itemObject.optString(SavedMediaStorageDefaults.MEDIA_TYPE_FIELD)
            if (mediaType.isBlank()) {
                mediaType = itemObject.optString(SavedMediaStorageDefaults.LEGACY_MEDIA_TYPE_FIELD)
            }

            var posterPath = itemObject.optString(SavedMediaStorageDefaults.POSTER_PATH_FIELD)
            if (posterPath.isBlank()) {
                posterPath = itemObject.optString(SavedMediaStorageDefaults.LEGACY_POSTER_PATH_FIELD)
            }

            savedMedia.add(
                TmdbMediaItem(
                    id = itemObject.optInt(SavedMediaStorageDefaults.ID_FIELD),
                    title = itemObject.optString(SavedMediaStorageDefaults.TITLE_FIELD).nullIfBlank(),
                    name = itemObject.optString(SavedMediaStorageDefaults.NAME_FIELD).nullIfBlank(),
                    mediaType = mediaType.nullIfBlank(),
                    overview = itemObject.optString(SavedMediaStorageDefaults.OVERVIEW_FIELD),
                    genreIds = parseGenreIds(
                        itemObject.optJSONArray(SavedMediaStorageDefaults.GENRE_IDS_FIELD)
                            ?: itemObject.optJSONArray(SavedMediaStorageDefaults.LEGACY_GENRE_IDS_FIELD)
                    ),
                    releaseDate = itemObject.optString(SavedMediaStorageDefaults.RELEASE_DATE_FIELD).nullIfBlank(),
                    posterPath = posterPath.nullIfBlank()
                )
            )
        }

        return savedMedia
    }

    fun saveSavedMedia(savedMedia: List<TmdbMediaItem>) {
        val savedMediaArray = JSONArray()

        savedMedia.forEach { item ->
            val itemObject = JSONObject()
            itemObject.put(SavedMediaStorageDefaults.ID_FIELD, item.id)
            itemObject.put(SavedMediaStorageDefaults.TITLE_FIELD, item.title)
            itemObject.put(SavedMediaStorageDefaults.NAME_FIELD, item.name)
            itemObject.put(SavedMediaStorageDefaults.MEDIA_TYPE_FIELD, item.mediaType)
            itemObject.put(SavedMediaStorageDefaults.OVERVIEW_FIELD, item.overview)
            itemObject.put(SavedMediaStorageDefaults.GENRE_IDS_FIELD, JSONArray(item.genreIds))
            itemObject.put(SavedMediaStorageDefaults.RELEASE_DATE_FIELD, item.releaseDate)
            itemObject.put(SavedMediaStorageDefaults.POSTER_PATH_FIELD, item.posterPath)
            savedMediaArray.put(itemObject)
        }

        sharedPreferences.edit {
            putString(SavedMediaStorageDefaults.SAVED_MEDIA_KEY, savedMediaArray.toString())
        }
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
    if (isBlank() || this == "null") {
        return null
    }

    return this
}

private object SavedMediaStorageDefaults {
    const val SAVED_MEDIA_KEY = "saved_media_json"
    const val ID_FIELD = "id"
    const val TITLE_FIELD = "title"
    const val NAME_FIELD = "name"
    const val MEDIA_TYPE_FIELD = "mediaType"
    const val OVERVIEW_FIELD = "overview"
    const val GENRE_IDS_FIELD = "genreIds"
    const val RELEASE_DATE_FIELD = "releaseDate"
    const val POSTER_PATH_FIELD = "posterPath"
    const val LEGACY_MEDIA_TYPE_FIELD = "media_type"
    const val LEGACY_GENRE_IDS_FIELD = "genre_ids"
    const val LEGACY_POSTER_PATH_FIELD = "poster_path"
}
