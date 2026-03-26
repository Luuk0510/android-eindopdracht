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
            savedMedia.add(
                TmdbMediaItem(
                    id = itemObject.optInt(SavedMediaStorageDefaults.ID_FIELD),
                    title = itemObject.optString(SavedMediaStorageDefaults.TITLE_FIELD).nullIfBlank(),
                    name = itemObject.optString(SavedMediaStorageDefaults.NAME_FIELD).nullIfBlank(),
                    mediaType = itemObject
                        .optString(SavedMediaStorageDefaults.MEDIA_TYPE_FIELD)
                        .ifBlank { itemObject.optString(SavedMediaStorageDefaults.LEGACY_MEDIA_TYPE_FIELD) }
                        .nullIfBlank(),
                    overview = itemObject.optString(SavedMediaStorageDefaults.OVERVIEW_FIELD),
                    genreIds = parseGenreIds(
                        itemObject.optJSONArray(SavedMediaStorageDefaults.GENRE_IDS_FIELD)
                            ?: itemObject.optJSONArray(SavedMediaStorageDefaults.LEGACY_GENRE_IDS_FIELD)
                    ),
                    releaseDate = itemObject.optString(SavedMediaStorageDefaults.RELEASE_DATE_FIELD).nullIfBlank(),
                    posterPath = itemObject
                        .optString(SavedMediaStorageDefaults.POSTER_PATH_FIELD)
                        .ifBlank { itemObject.optString(SavedMediaStorageDefaults.LEGACY_POSTER_PATH_FIELD) }
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
                    put(SavedMediaStorageDefaults.ID_FIELD, item.id)
                    put(SavedMediaStorageDefaults.TITLE_FIELD, item.title)
                    put(SavedMediaStorageDefaults.NAME_FIELD, item.name)
                    put(SavedMediaStorageDefaults.MEDIA_TYPE_FIELD, item.mediaType)
                    put(SavedMediaStorageDefaults.OVERVIEW_FIELD, item.overview)
                    put(SavedMediaStorageDefaults.GENRE_IDS_FIELD, JSONArray(item.genreIds))
                    put(SavedMediaStorageDefaults.RELEASE_DATE_FIELD, item.releaseDate)
                    put(SavedMediaStorageDefaults.POSTER_PATH_FIELD, item.posterPath)
                }
            )
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
    return takeUnless { it.isBlank() || it == "null" }
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
