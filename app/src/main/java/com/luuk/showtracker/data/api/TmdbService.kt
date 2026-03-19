package com.luuk.showtracker.data.api

import com.luuk.showtracker.data.model.TmdbResponse
import retrofit2.http.GET

interface TmdbService {
    @GET("trending/all/day")
    suspend fun getTrending(): TmdbResponse
}