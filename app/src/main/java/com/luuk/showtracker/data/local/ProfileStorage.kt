package com.luuk.showtracker.data.local

import android.content.Context
import androidx.core.content.edit
import com.luuk.showtracker.data.model.UserProfile

class ProfileStorage(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences("profile_storage", Context.MODE_PRIVATE)

    fun loadProfile(): UserProfile {
        return UserProfile(
            name = sharedPreferences.getString(ProfileStorageDefaults.NAME_KEY, null)
                ?: ProfileStorageDefaults.DEFAULT_NAME,
            photoUri = sharedPreferences.getString(ProfileStorageDefaults.PHOTO_URI_KEY, null)
        )
    }

    fun saveProfile(profile: UserProfile) {
        sharedPreferences.edit {
            putString(ProfileStorageDefaults.NAME_KEY, profile.name)
            putString(ProfileStorageDefaults.PHOTO_URI_KEY, profile.photoUri)
        }
    }
}

private object ProfileStorageDefaults {
    const val NAME_KEY = "profile_name"
    const val PHOTO_URI_KEY = "profile_photo_uri"
    const val DEFAULT_NAME = "Guest"
}
