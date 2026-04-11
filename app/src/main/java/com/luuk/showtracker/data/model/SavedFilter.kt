package com.luuk.showtracker.data.model

import com.luuk.showtracker.R

enum class SavedFilter(val labelResId: Int) {
    ALL(R.string.saved_filter_all),
    WATCHED(R.string.saved_filter_watched),
    UNWATCHED(R.string.saved_filter_unwatched)
}
