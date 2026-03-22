package com.luuk.showtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luuk.showtracker.data.api.RetrofitClient
import com.luuk.showtracker.data.repository.MediaRepository
import com.luuk.showtracker.ui.navigation.ShowTrackerApp
import com.luuk.showtracker.ui.theme.ShowTrackerTheme
import com.luuk.showtracker.ui.viewmodel.MediaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val repository = MediaRepository(RetrofitClient.tmdbService)
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MediaViewModel(repository) as T
            }
        }

        enableEdgeToEdge()
        setContent {
            ShowTrackerTheme {
                val mediaViewModel: MediaViewModel = viewModel(factory = viewModelFactory)
                ShowTrackerApp(viewModel = mediaViewModel)
            }
        }
    }
}
