package com.luuk.showtracker.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WatchedStorage(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences("watched_storage", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun loadWatchedIds(): Set<Int> {
        val json = sharedPreferences.getString("watched_ids_json", null) ?: return emptySet()
        val type = object : TypeToken<Set<Int>>() {}.type
        return gson.fromJson(json, type) ?: emptySet()
    }

    fun saveWatchedIds(watchedIds: Set<Int>) {
        val json = gson.toJson(watchedIds)
        sharedPreferences.edit().putString("watched_ids_json", json).apply()
    }
}
