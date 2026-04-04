package com.luuk.showtracker.data.local

import android.content.Context
import androidx.core.content.edit
import com.luuk.showtracker.data.model.WatchlistSortOption

class WatchlistPreferences(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences(WatchlistPreferencesDefaults.PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun loadSortOption(): WatchlistSortOption {
        val storedValue = sharedPreferences.getString(WatchlistPreferencesDefaults.SORT_OPTION_KEY, null)
        return WatchlistSortOption.fromStorageValue(storedValue)
    }

    fun saveSortOption(sortOption: WatchlistSortOption) {
        sharedPreferences.edit {
            putString(WatchlistPreferencesDefaults.SORT_OPTION_KEY, sortOption.storageValue)
        }
    }
}

private object WatchlistPreferencesDefaults {
    const val PREFERENCES_NAME = "watchlist_preferences"
    const val SORT_OPTION_KEY = "sort_option"
}
