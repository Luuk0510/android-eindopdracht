package com.luuk.showtracker.data.model

data class MediaReview(
    val mediaId: Int,
    val title: String,
    val reviewText: String,
    val rating: Int,
    val dateTime: String
)
