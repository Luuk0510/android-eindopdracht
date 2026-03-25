package com.luuk.showtracker.data.repository

import com.luuk.showtracker.BuildConfig
import com.luuk.showtracker.data.api.TmdbService
import com.luuk.showtracker.data.model.TmdbMediaItem

class MediaRepository(private val tmdbService: TmdbService) {
    
    suspend fun getTrendingMedia(page: Int): Result<List<TmdbMediaItem>> {
        return try {
            val mediaItems = tmdbService.getTrending(
                apiKey = BuildConfig.TMDB_API_KEY, 
                page = page
            )
            Result.success(mediaItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchMedia(query: String): Result<List<TmdbMediaItem>> {
        return try {
            val mediaItems = tmdbService.searchMedia(
                apiKey = BuildConfig.TMDB_API_KEY,
                query = query
            )
            Result.success(mediaItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
