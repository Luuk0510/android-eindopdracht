package com.luuk.showtracker.data.model

data class TmdbResponse(
    val results: List<TmdbMediaItem>
)

data class TmdbMediaItem(
    val id: Int,
    val title: String?,
    val name: String?,
    val mediaType: String? = null,
    val overview: String,
    val genreIds: List<Int> = emptyList(),
    val releaseDate: String? = null,
    val posterPath: String?
)
