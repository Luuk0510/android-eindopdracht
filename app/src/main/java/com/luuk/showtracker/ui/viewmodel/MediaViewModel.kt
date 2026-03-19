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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var currentPage = 1
    private var isLastPage = false

    init {
        loadNextPage()
    }

    fun loadNextPage() {
        if (_isLoading.value || isLastPage) return

        viewModelScope.launch {
            _isLoading.value = true
            repository.getTrendingMedia(currentPage)
                .onSuccess { newItems ->
                    if (newItems.isEmpty()) {
                        isLastPage = true
                    } else {
                        _mediaItems.value = _mediaItems.value + newItems
                        currentPage++
                    }
                    _errorMessage.value = null
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Unknown error occurred"
                }
            _isLoading.value = false
        }
    }
}