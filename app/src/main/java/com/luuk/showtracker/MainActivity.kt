package com.luuk.showtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luuk.showtracker.data.api.TmdbService
import com.luuk.showtracker.data.local.ProfileStorage
import com.luuk.showtracker.data.local.ReviewStorage
import com.luuk.showtracker.data.local.SavedMediaStorage
import com.luuk.showtracker.data.local.WatchlistPreferences
import com.luuk.showtracker.data.local.WatchedStorage
import com.luuk.showtracker.ui.navigation.ShowTrackerApp
import com.luuk.showtracker.ui.theme.ShowTrackerTheme
import com.luuk.showtracker.ui.viewmodel.MediaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModelFactory = createMediaViewModelFactory()

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            ShowTrackerTheme {
                val mediaViewModel: MediaViewModel = viewModel(factory = viewModelFactory)
                ShowTrackerApp(viewModel = mediaViewModel)
            }
        }
    }

    private fun createMediaViewModelFactory(): ViewModelProvider.Factory {
        val tmdbService = TmdbService(applicationContext)
        val profileStorage = ProfileStorage(applicationContext)
        val reviewStorage = ReviewStorage(applicationContext)
        val savedMediaStorage = SavedMediaStorage(applicationContext)
        val watchlistPreferences = WatchlistPreferences(applicationContext)
        val watchedStorage = WatchedStorage(applicationContext)

        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MediaViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return MediaViewModel(
                        tmdbService,
                        profileStorage,
                        reviewStorage,
                        savedMediaStorage,
                        watchlistPreferences,
                        watchedStorage
                    ) as T
                }

                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
