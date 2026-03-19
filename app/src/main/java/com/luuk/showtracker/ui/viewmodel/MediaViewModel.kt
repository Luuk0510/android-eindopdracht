package com.luuk.showtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luuk.showtracker.data.model.TmdbMediaItem
import com.luuk.showtracker.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MediaViewModel(private val repository: MediaRepository) : ViewModel() {

    private val _mediaItems = MutableStateFlow<List<TmdbMediaItem>>(emptyList())
    val mediaItems: StateFlow<List<TmdbMediaItem>> = _mediaItems.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchTrendingMedia()
    }

    fun fetchTrendingMedia() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getTrendingMedia()
                .onSuccess { items ->
                    _mediaItems.value = items
                    _errorMessage.value = null
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Unknown error occurred"
                }
            _isLoading.value = false
        }
    }
}