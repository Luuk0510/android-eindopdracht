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
        val viewModelFactory = createViewModelFactory()

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

    private fun createViewModelFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MediaViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return createMediaViewModel() as T
                }

                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    private fun createMediaViewModel(): MediaViewModel {
        return MediaViewModel(
            tmdbService = TmdbService(applicationContext),
            profileStorage = ProfileStorage(applicationContext),
            reviewStorage = ReviewStorage(applicationContext),
            savedMediaStorage = SavedMediaStorage(applicationContext),
            watchlistPreferences = WatchlistPreferences(applicationContext),
            watchedStorage = WatchedStorage(applicationContext)
        )
    }
}
