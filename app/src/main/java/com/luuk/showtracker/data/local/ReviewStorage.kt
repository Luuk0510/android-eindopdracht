package com.luuk.showtracker.data.local

import android.content.Context
import androidx.core.content.edit
import com.luuk.showtracker.data.model.MediaReview
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

class ReviewStorage(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("review_storage", Context.MODE_PRIVATE)

    fun loadReviews(): Map<Int, MediaReview> {
        val json = sharedPreferences.getString(ReviewStorageDefaults.REVIEWS_KEY, null) ?: return emptyMap()
        val reviews = mutableMapOf<Int, MediaReview>()
        val parsedJson = try {
            JSONTokener(json).nextValue()
        } catch (_: Exception) {
            return emptyMap()
        }

        when (parsedJson) {
            is JSONArray -> {
                for (index in 0 until parsedJson.length()) {
                    val reviewObject = parsedJson.optJSONObject(index) ?: continue
                    val review = parseReview(reviewObject)
                    if (review != null) {
                        reviews[review.mediaId] = review
                    }
                }
            }

            is JSONObject -> {
                val keys = parsedJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val reviewObject = parsedJson.optJSONObject(key) ?: continue
                    val review = parseReview(reviewObject)
                    if (review != null) {
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
            val reviewObject = JSONObject()
            reviewObject.put(ReviewStorageDefaults.MEDIA_ID_FIELD, review.mediaId)
            reviewObject.put(ReviewStorageDefaults.TITLE_FIELD, review.title)
            reviewObject.put(ReviewStorageDefaults.REVIEW_TEXT_FIELD, review.reviewText)
            reviewObject.put(ReviewStorageDefaults.RATING_FIELD, review.rating)
            reviewObject.put(ReviewStorageDefaults.DATE_TIME_FIELD, review.dateTime)
            reviewArray.put(reviewObject)
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
