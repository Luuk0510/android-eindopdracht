package com.luuk.showtracker.data.local

import android.content.Context

class WatchedStorage(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences("watched_storage", Context.MODE_PRIVATE)

    fun loadWatchedIds(): Set<Int> {
        return sharedPreferences
            .getStringSet(WatchedStorageDefaults.WatchedIdsKey, emptySet())
            .orEmpty()
            .mapNotNull { it.toIntOrNull() }
            .toSet()
    }

    fun saveWatchedIds(watchedIds: Set<Int>) {
        sharedPreferences.edit()
            .putStringSet(
                WatchedStorageDefaults.WatchedIdsKey,
                watchedIds.map { it.toString() }.toSet()
            )
            .apply()
    }
}

private object WatchedStorageDefaults {
    const val WatchedIdsKey = "watched_ids"
}
