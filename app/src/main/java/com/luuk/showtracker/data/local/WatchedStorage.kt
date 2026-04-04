package com.luuk.showtracker.data.local

import android.content.Context
import androidx.core.content.edit

class WatchedStorage(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences("watched_storage", Context.MODE_PRIVATE)

    fun loadWatchedIds(): Set<Int> {
        return sharedPreferences
            .getStringSet(WatchedStorageDefaults.WATCHED_IDS_KEY, emptySet())
            .orEmpty()
            .mapNotNull { it.toIntOrNull() }
            .toSet()
    }

    fun saveWatchedIds(watchedIds: Set<Int>) {
        sharedPreferences.edit {
            putStringSet(
                WatchedStorageDefaults.WATCHED_IDS_KEY,
                watchedIds.map { it.toString() }.toSet()
            )
        }
    }
}

private object WatchedStorageDefaults {
    const val WATCHED_IDS_KEY = "watched_ids"
}
