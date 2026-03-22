package com.luuk.showtracker.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.luuk.showtracker.data.model.MediaReview

class ReviewStorage(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences("review_storage", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun loadReviews(): Map<Int, MediaReview> {
        val json = sharedPreferences.getString("reviews_json", null) ?: return emptyMap()
        val type = object : TypeToken<Map<Int, MediaReview>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }

    fun saveReviews(reviews: Map<Int, MediaReview>) {
        val json = gson.toJson(reviews)
        sharedPreferences.edit().putString("reviews_json", json).apply()
    }
}
