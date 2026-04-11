package com.luuk.showtracker.ui.navigation

import com.luuk.showtracker.R
import com.luuk.showtracker.data.model.WatchlistSortOption
import org.junit.Assert.assertEquals
import org.junit.Test

class WatchlistSortLabelTest {

    @Test
    fun labelResId_returnsLabelForEachSortOption() {
        assertEquals(R.string.sort_newest, WatchlistSortOption.NEWEST.labelResId())
        assertEquals(R.string.sort_oldest, WatchlistSortOption.OLDEST.labelResId())
        assertEquals(R.string.sort_title_asc, WatchlistSortOption.TITLE_ASC.labelResId())
        assertEquals(R.string.sort_title_desc, WatchlistSortOption.TITLE_DESC.labelResId())
    }
}
