package com.luuk.showtracker.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.luuk.showtracker.data.model.TmdbMediaItem

class SavedMediaStorage(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences("saved_media_storage", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun loadSavedMedia(): List<TmdbMediaItem> {
        val json = sharedPreferences.getString("saved_media_json", null) ?: return emptyList()
        val type = object : TypeToken<List<TmdbMediaItem>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun saveSavedMedia(savedMedia: List<TmdbMediaItem>) {
        val json = gson.toJson(savedMedia)
        sharedPreferences.edit().putString("saved_media_json", json).apply()
    }
}
