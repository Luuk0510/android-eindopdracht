package com.luuk.showtracker.data.local

import android.content.Context
import com.luuk.showtracker.data.model.MediaReview
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

class ReviewStorage(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences("review_storage", Context.MODE_PRIVATE)

    fun loadReviews(): Map<Int, MediaReview> {
        val json = sharedPreferences.getString(ReviewStorageDefaults.ReviewsKey, null) ?: return emptyMap()
        val reviews = mutableMapOf<Int, MediaReview>()
        val parsedJson = runCatching { JSONTokener(json).nextValue() }.getOrNull() ?: return emptyMap()

        when (parsedJson) {
            is JSONArray -> {
                for (index in 0 until parsedJson.length()) {
                    val reviewObject = parsedJson.optJSONObject(index) ?: continue
                    parseReview(reviewObject)?.let { review ->
                        reviews[review.mediaId] = review
                    }
                }
            }

            is JSONObject -> {
                val keys = parsedJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val reviewObject = parsedJson.optJSONObject(key) ?: continue
                    parseReview(reviewObject)?.let { review ->
                        reviews[review.mediaId] = review
                    }
                }
            }
        }

        return reviews
    }

    fun saveReviews(reviews: Map<Int, MediaReview>) {
        val reviewArray = JSONArray()

        reviews.values.forEach { review ->
            reviewArray.put(
                JSONObject().apply {
                    put(ReviewStorageDefaults.MediaIdField, review.mediaId)
                    put(ReviewStorageDefaults.TitleField, review.title)
                    put(ReviewStorageDefaults.ReviewTextField, review.reviewText)
                    put(ReviewStorageDefaults.RatingField, review.rating)
                    put(ReviewStorageDefaults.DateTimeField, review.dateTime)
                }
            )
        }

        sharedPreferences.edit()
            .putString(ReviewStorageDefaults.ReviewsKey, reviewArray.toString())
            .apply()
    }

    private fun parseReview(reviewObject: JSONObject): MediaReview? {
        val mediaId = reviewObject.optInt(ReviewStorageDefaults.MediaIdField)
        if (mediaId == 0) return null

        return MediaReview(
            mediaId = mediaId,
            title = reviewObject.optString(ReviewStorageDefaults.TitleField),
            reviewText = reviewObject.optString(ReviewStorageDefaults.ReviewTextField),
            rating = reviewObject.optInt(ReviewStorageDefaults.RatingField),
            dateTime = reviewObject.optString(ReviewStorageDefaults.DateTimeField)
        )
    }
}

private object ReviewStorageDefaults {
    const val ReviewsKey = "reviews_json"
    const val MediaIdField = "mediaId"
    const val TitleField = "title"
    const val ReviewTextField = "reviewText"
    const val RatingField = "rating"
    const val DateTimeField = "dateTime"
}
