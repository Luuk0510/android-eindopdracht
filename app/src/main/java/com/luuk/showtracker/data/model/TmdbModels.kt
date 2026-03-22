package com.luuk.showtracker.data.model

import com.google.gson.annotations.SerializedName

data class TmdbResponse(
    val results: List<TmdbMediaItem>
)

data class TmdbMediaItem(
    val id: Int,
    val title: String?, // Used by Movies
    val name: String?,  // Used by TV Series
    val overview: String,
    @SerializedName("poster_path")
    val posterPath: String?
)
