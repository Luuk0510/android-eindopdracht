package com.luuk.showtracker.data.api

import com.luuk.showtracker.data.model.TmdbResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbService {
    @GET("trending/all/day")
    suspend fun getTrending(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int
    ): TmdbResponse

    @GET("search/multi")
    suspend fun searchMedia(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): TmdbResponse
}
