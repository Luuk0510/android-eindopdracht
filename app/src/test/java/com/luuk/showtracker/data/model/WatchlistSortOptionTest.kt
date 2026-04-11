package com.luuk.showtracker.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WatchlistSortOptionTest {

    @Test
    fun fromStorageValue_returnsMatchingSortOption() {
        val sortOption = WatchlistSortOption.fromStorageValue("title_desc")

        assertEquals(WatchlistSortOption.TITLE_DESC, sortOption)
    }

    @Test
    fun fromStorageValue_returnsNewestForUnknownValue() {
        val sortOption = WatchlistSortOption.fromStorageValue("wrong_value")

        assertEquals(WatchlistSortOption.NEWEST, sortOption)
    }

    @Test
    fun fromStorageValue_returnsNewestForNullValue() {
        val sortOption = WatchlistSortOption.fromStorageValue(null)

        assertEquals(WatchlistSortOption.NEWEST, sortOption)
    }
}
