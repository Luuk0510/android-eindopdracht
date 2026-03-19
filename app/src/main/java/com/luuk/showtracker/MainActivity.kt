package com.luuk.showtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luuk.showtracker.data.api.RetrofitClient
import com.luuk.showtracker.data.repository.MediaRepository
import com.luuk.showtracker.ui.screen.MediaListScreen
import com.luuk.showtracker.ui.theme.ShowTrackerTheme
import com.luuk.showtracker.ui.viewmodel.MediaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Simple manual dependency injection
        val repository = MediaRepository(RetrofitClient.tmdbService)
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MediaViewModel(repository) as T
            }
        }

        enableEdgeToEdge()
        setContent {
            ShowTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val mediaViewModel: MediaViewModel = viewModel(factory = viewModelFactory)
                    MediaListScreen(
                        viewModel = mediaViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}