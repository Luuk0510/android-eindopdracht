package com.luuk.showtracker.data.repository

import com.luuk.showtracker.data.api.TmdbService
import com.luuk.showtracker.data.model.TmdbMediaItem

class MediaRepository(private val tmdbService: TmdbService) {
    
    suspend fun getTrendingMedia(): Result<List<TmdbMediaItem>> {
        return try {
            // No need to pass api_key here anymore!
            val response = tmdbService.getTrending()
            Result.success(response.results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}