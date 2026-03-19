package com.luuk.showtracker.data.repository

import com.luuk.showtracker.data.api.TmdbService
import com.luuk.showtracker.data.model.TmdbMediaItem

class MediaRepository(private val tmdbService: TmdbService) {
    
    suspend fun getTrendingMedia(page: Int): Result<List<TmdbMediaItem>> {
        return try {
            val response = tmdbService.getTrending(page)
            Result.success(response.results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}