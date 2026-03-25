package com.luuk.showtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luuk.showtracker.data.local.ProfileStorage
import com.luuk.showtracker.data.local.ReviewStorage
import com.luuk.showtracker.data.local.SavedMediaStorage
import com.luuk.showtracker.data.local.WatchlistPreferences
import com.luuk.showtracker.data.local.WatchedStorage
import com.luuk.showtracker.data.model.MediaReview
import com.luuk.showtracker.data.model.TmdbMediaItem
import com.luuk.showtracker.data.model.UserProfile
import com.luuk.showtracker.data.model.WatchlistSortOption
import com.luuk.showtracker.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MediaViewModel(
    private val repository: MediaRepository,
    private val profileStorage: ProfileStorage,
    private val reviewStorage: ReviewStorage,
    private val savedMediaStorage: SavedMediaStorage,
    private val watchlistPreferences: WatchlistPreferences,
    private val watchedStorage: WatchedStorage
) : ViewModel() {

    private val _mediaItems = MutableStateFlow<List<TmdbMediaItem>>(emptyList())
    val mediaItems: StateFlow<List<TmdbMediaItem>> = _mediaItems.asStateFlow()

    private val _searchResults = MutableStateFlow<List<TmdbMediaItem>>(emptyList())
    val searchResults: StateFlow<List<TmdbMediaItem>> = _searchResults.asStateFlow()

    private val _savedItems = MutableStateFlow(savedMediaStorage.loadSavedMedia())
    val savedItems: StateFlow<List<TmdbMediaItem>> = _savedItems.asStateFlow()

    private val _profile = MutableStateFlow(profileStorage.loadProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private val _reviews = MutableStateFlow(reviewStorage.loadReviews())
    val reviews: StateFlow<Map<Int, MediaReview>> = _reviews.asStateFlow()

    private val _watchlistSortOption = MutableStateFlow(watchlistPreferences.loadSortOption())
    val watchlistSortOption: StateFlow<WatchlistSortOption> = _watchlistSortOption.asStateFlow()

    private val _watchedIds = MutableStateFlow(watchedStorage.loadWatchedIds())
    val watchedIds: StateFlow<Set<Int>> = _watchedIds.asStateFlow()

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
                        _mediaItems.value += newItems
                        currentPage++
                    }
                    clearError()
                }
                .onFailure { error ->
                    setError(error)
                }
            _isLoading.value = false
        }
    }

    fun searchMedia(query: String) {
        searchJob?.cancel()

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            clearError()
            return
        }

        searchJob = viewModelScope.launch {
            delay(MediaViewModelDefaults.SEARCH_DEBOUNCE_MS)
            _isLoading.value = true
            repository.searchMedia(query)
                .onSuccess { results ->
                    _searchResults.value = results
                    clearError()
                }
                .onFailure { error ->
                    _searchResults.value = emptyList()
                    setError(error)
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
        saveCurrentSavedItems()
    }

    fun saveProfile(name: String, photoUri: String?) {
        val updatedProfile = UserProfile(
            name = name.ifBlank { MediaViewModelDefaults.DEFAULT_PROFILE_NAME },
            photoUri = photoUri
        )
        _profile.value = updatedProfile
        profileStorage.saveProfile(updatedProfile)
    }

    fun toggleWatched(itemId: Int) {
        _watchedIds.value = _watchedIds.value.toMutableSet().apply {
            if (contains(itemId)) remove(itemId) else add(itemId)
        }
        saveCurrentWatchedIds()
    }

    fun setWatchlistSortOption(sortOption: WatchlistSortOption) {
        _watchlistSortOption.value = sortOption
        watchlistPreferences.saveSortOption(sortOption)
    }

    fun saveReview(
        itemId: Int,
        title: String,
        reviewText: String,
        rating: Int
    ) {
        val review = MediaReview(
            mediaId = itemId,
            title = title,
            reviewText = reviewText,
            rating = rating,
            dateTime = createReviewDateTime()
        )
        _reviews.value = _reviews.value.toMutableMap().apply {
            this[itemId] = review
        }
        saveCurrentReviews()
    }

    fun deleteReview(itemId: Int) {
        _reviews.value = _reviews.value.toMutableMap().apply {
            remove(itemId)
        }
        saveCurrentReviews()
    }

    fun getMediaItemById(itemId: Int): TmdbMediaItem? {
        return _savedItems.value.firstOrNull { it.id == itemId }
            ?: _searchResults.value.firstOrNull { it.id == itemId }
            ?: _mediaItems.value.firstOrNull { it.id == itemId }
    }

    fun createFallbackMediaItem(itemId: Int): TmdbMediaItem {
        return TmdbMediaItem(
            id = itemId,
            title = "",
            name = null,
            overview = "",
            posterPath = null
        )
    }

    private fun clearError() {
        _errorMessage.value = null
    }

    private fun setError(error: Throwable) {
        _errorMessage.value = error.message ?: MediaViewModelDefaults.UNKNOWN_ERROR_MESSAGE
    }

    private fun saveCurrentSavedItems() {
        savedMediaStorage.saveSavedMedia(_savedItems.value)
    }

    private fun saveCurrentWatchedIds() {
        watchedStorage.saveWatchedIds(_watchedIds.value)
    }

    private fun saveCurrentReviews() {
        reviewStorage.saveReviews(_reviews.value)
    }

    private fun createReviewDateTime(): String {
        return LocalDateTime.now().format(
            DateTimeFormatter.ofPattern(MediaViewModelDefaults.REVIEW_DATE_TIME_PATTERN)
        )
    }
}

private object MediaViewModelDefaults {
    const val DEFAULT_PROFILE_NAME = "Guest"
    const val UNKNOWN_ERROR_MESSAGE = "Unknown error occurred"
    const val REVIEW_DATE_TIME_PATTERN = "dd-MM-yyyy HH:mm"
    const val SEARCH_DEBOUNCE_MS = 300L
}
