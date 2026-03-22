package com.luuk.showtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luuk.showtracker.data.model.TmdbMediaItem
import com.luuk.showtracker.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MediaViewModel(private val repository: MediaRepository) : ViewModel() {

    private val _mediaItems = MutableStateFlow<List<TmdbMediaItem>>(emptyList())
    val mediaItems: StateFlow<List<TmdbMediaItem>> = _mediaItems.asStateFlow()

    private val _searchResults = MutableStateFlow<List<TmdbMediaItem>>(emptyList())
    val searchResults: StateFlow<List<TmdbMediaItem>> = _searchResults.asStateFlow()

    private val _savedItems = MutableStateFlow<List<TmdbMediaItem>>(emptyList())
    val savedItems: StateFlow<List<TmdbMediaItem>> = _savedItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var currentPage = 1
    private var isLastPage = false
    private var searchJob: Job? = null

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

    fun searchMedia(query: String) {
        searchJob?.cancel()

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _errorMessage.value = null
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            _isLoading.value = true
            repository.searchMedia(query)
                .onSuccess { results ->
                    _searchResults.value = results
                    _errorMessage.value = null
                }
                .onFailure { error ->
                    _searchResults.value = emptyList()
                    _errorMessage.value = error.message ?: "Unknown error occurred"
                }
            _isLoading.value = false
        }
    }

    fun toggleSaved(item: TmdbMediaItem) {
        val currentSavedItems = _savedItems.value
        val isAlreadySaved = currentSavedItems.any { it.id == item.id }

        _savedItems.value = if (isAlreadySaved) {
            currentSavedItems.filterNot { it.id == item.id }
        } else {
            listOf(item) + currentSavedItems
        }
    }

    fun isSaved(itemId: Int): Boolean {
        return _savedItems.value.any { it.id == itemId }
    }
}
