package com.luuk.showtracker.data.local

import android.content.Context
import com.luuk.showtracker.data.model.UserProfile

class ProfileStorage(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences("profile_storage", Context.MODE_PRIVATE)

    fun loadProfile(): UserProfile {
        return UserProfile(
            name = sharedPreferences.getString(ProfileStorageDefaults.NameKey, null)
                ?: ProfileStorageDefaults.DefaultName,
            photoUri = sharedPreferences.getString(ProfileStorageDefaults.PhotoUriKey, null)
        )
    }

    fun saveProfile(profile: UserProfile) {
        sharedPreferences.edit()
            .putString(ProfileStorageDefaults.NameKey, profile.name)
            .putString(ProfileStorageDefaults.PhotoUriKey, profile.photoUri)
            .apply()
    }
}

private object ProfileStorageDefaults {
    const val NameKey = "profile_name"
    const val PhotoUriKey = "profile_photo_uri"
    const val DefaultName = "Guest"
}
