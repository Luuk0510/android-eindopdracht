package com.luuk.showtracker.data.model

enum class WatchlistSortOption(
    val storageValue: String
) {
    NEWEST("newest"),
    OLDEST("oldest"),
    TITLE_ASC("title_asc"),
    TITLE_DESC("title_desc");

    companion object {
        fun fromStorageValue(value: String?): WatchlistSortOption {
            return entries.firstOrNull { it.storageValue == value } ?: NEWEST
        }
    }
}
