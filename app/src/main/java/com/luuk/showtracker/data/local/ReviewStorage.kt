package com.luuk.showtracker.data.local

import android.content.Context
import androidx.core.content.edit
import com.luuk.showtracker.data.model.MediaReview
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

class ReviewStorage(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences("review_storage", Context.MODE_PRIVATE)

    fun loadReviews(): Map<Int, MediaReview> {
        val json = sharedPreferences.getString(ReviewStorageDefaults.REVIEWS_KEY, null) ?: return emptyMap()
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
                    put(ReviewStorageDefaults.MEDIA_ID_FIELD, review.mediaId)
                    put(ReviewStorageDefaults.TITLE_FIELD, review.title)
                    put(ReviewStorageDefaults.REVIEW_TEXT_FIELD, review.reviewText)
                    put(ReviewStorageDefaults.RATING_FIELD, review.rating)
                    put(ReviewStorageDefaults.DATE_TIME_FIELD, review.dateTime)
                }
            )
        }

        sharedPreferences.edit {
            putString(ReviewStorageDefaults.REVIEWS_KEY, reviewArray.toString())
        }
    }

    private fun parseReview(reviewObject: JSONObject): MediaReview? {
        val mediaId = reviewObject.optInt(ReviewStorageDefaults.MEDIA_ID_FIELD)
        if (mediaId == 0) return null

        return MediaReview(
            mediaId = mediaId,
            title = reviewObject.optString(ReviewStorageDefaults.TITLE_FIELD),
            reviewText = reviewObject.optString(ReviewStorageDefaults.REVIEW_TEXT_FIELD),
            rating = reviewObject.optInt(ReviewStorageDefaults.RATING_FIELD),
            dateTime = reviewObject.optString(ReviewStorageDefaults.DATE_TIME_FIELD)
        )
    }
}

private object ReviewStorageDefaults {
    const val REVIEWS_KEY = "reviews_json"
    const val MEDIA_ID_FIELD = "mediaId"
    const val TITLE_FIELD = "title"
    const val REVIEW_TEXT_FIELD = "reviewText"
    const val RATING_FIELD = "rating"
    const val DATE_TIME_FIELD = "dateTime"
}
