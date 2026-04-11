package com.luuk.showtracker.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GenreUtilsTest {

    @Test
    fun genreNames_returnsMovieGenresForMovieItem() {
        val item = TmdbMediaItem(
            id = 1,
            title = "Movie title",
            name = null,
            mediaType = "movie",
            overview = "Overview",
            genreIds = listOf(28, 35, 999),
            posterPath = null
        )

        val genreNames = item.genreNames()

        assertEquals(listOf("Action", "Comedy"), genreNames)
    }

    @Test
    fun genreNames_returnsTvGenresForTvItem() {
        val item = TmdbMediaItem(
            id = 2,
            title = null,
            name = "TV title",
            mediaType = "tv",
            overview = "Overview",
            genreIds = listOf(10759, 10765),
            posterPath = null
        )

        val genreNames = item.genreNames()

        assertEquals(listOf("Action & Adventure", "Sci-Fi & Fantasy"), genreNames)
    }

    @Test
    fun genreNames_removesDuplicateGenreNames() {
        val item = TmdbMediaItem(
            id = 3,
            title = "Movie title",
            name = null,
            mediaType = "movie",
            overview = "Overview",
            genreIds = listOf(28, 28, 35),
            posterPath = null
        )

        val genreNames = item.genreNames()

        assertEquals(listOf("Action", "Comedy"), genreNames)
    }

    @Test
    fun genreNames_usesMovieGenresWhenMediaTypeIsMissingButTitleExists() {
        val item = TmdbMediaItem(
            id = 4,
            title = "Movie title",
            name = null,
            mediaType = null,
            overview = "Overview",
            genreIds = listOf(878),
            posterPath = null
        )

        val genreNames = item.genreNames()

        assertEquals(listOf("Science Fiction"), genreNames)
    }

    @Test
    fun genreNames_usesTvGenresWhenMediaTypeAndTitleAreMissing() {
        val item = TmdbMediaItem(
            id = 5,
            title = null,
            name = "TV title",
            mediaType = null,
            overview = "Overview",
            genreIds = listOf(10765),
            posterPath = null
        )

        val genreNames = item.genreNames()

        assertEquals(listOf("Sci-Fi & Fantasy"), genreNames)
    }

    @Test
    fun genreNames_returnsEmptyListForUnknownGenres() {
        val item = TmdbMediaItem(
            id = 6,
            title = "Movie title",
            name = null,
            mediaType = "movie",
            overview = "Overview",
            genreIds = listOf(999, 1000),
            posterPath = null
        )

        val genreNames = item.genreNames()

        assertEquals(emptyList<String>(), genreNames)
    }
}
