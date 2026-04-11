package com.luuk.showtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luuk.showtracker.BuildConfig
import com.luuk.showtracker.R
import com.luuk.showtracker.data.api.TmdbService
import com.luuk.showtracker.data.local.ProfileStorage
import com.luuk.showtracker.data.local.ReviewStorage
import com.luuk.showtracker.data.local.SavedMediaStorage
import com.luuk.showtracker.data.local.WatchedStorage
import com.luuk.showtracker.data.local.WatchlistPreferences
import com.luuk.showtracker.data.model.MediaReview
import com.luuk.showtracker.data.model.TmdbMediaItem
import com.luuk.showtracker.data.model.UserProfile
import com.luuk.showtracker.data.model.WatchlistSortOption
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MediaViewModel(
    private val tmdbService: TmdbService,
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

    private val _isTrendingLoading = MutableStateFlow(false)
    val isTrendingLoading: StateFlow<Boolean> = _isTrendingLoading.asStateFlow()

    private val _isSearchLoading = MutableStateFlow(false)
    val isSearchLoading: StateFlow<Boolean> = _isSearchLoading.asStateFlow()

    private val _trendingErrorMessage = MutableStateFlow<String?>(null)
    val trendingErrorMessage: StateFlow<String?> = _trendingErrorMessage.asStateFlow()

    private val _searchErrorMessage = MutableStateFlow<String?>(null)
    val searchErrorMessage: StateFlow<String?> = _searchErrorMessage.asStateFlow()

    private val _snackbarMessages = MutableSharedFlow<Int>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val snackbarMessages: SharedFlow<Int> = _snackbarMessages

    private var currentPage = 1
    private var isLastPage = false
    private var searchJob: Job? = null
    private var selectedMediaItem: TmdbMediaItem? = null

    init {
        loadNextPage()
    }

    fun loadNextPage() {
        if (_isTrendingLoading.value || isLastPage) return

        viewModelScope.launch {
            _isTrendingLoading.value = true
            try {
                val newItems = tmdbService.getTrending(
                    apiKey = BuildConfig.TMDB_API_KEY,
                    page = currentPage
                )
                if (newItems.isEmpty()) {
                    isLastPage = true
                } else {
                    _mediaItems.value += newItems
                    currentPage++
                }
                _trendingErrorMessage.value = null
            } catch (error: Exception) {
                _trendingErrorMessage.value = error.message ?: UNKNOWN_ERROR_MESSAGE
            } finally {
                _isTrendingLoading.value = false
            }
        }
    }

    fun refreshTrending() {
        if (_isTrendingLoading.value) return

        _mediaItems.value = emptyList()
        currentPage = 1
        isLastPage = false
        _trendingErrorMessage.value = null
        loadNextPage()
    }

    fun searchMedia(query: String) {
        searchJob?.cancel()

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isSearchLoading.value = false
            _searchErrorMessage.value = null
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            _isSearchLoading.value = true

            try {
                _searchResults.value = tmdbService.searchMedia(
                    apiKey = BuildConfig.TMDB_API_KEY,
                    query = query
                )
                _searchErrorMessage.value = null
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _searchResults.value = emptyList()
                _searchErrorMessage.value = error.message ?: UNKNOWN_ERROR_MESSAGE
            } finally {
                _isSearchLoading.value = false
            }
        }
    }

    fun toggleSaved(item: TmdbMediaItem) {
        val currentSavedItems = _savedItems.value
        val isAlreadySaved = currentSavedItems.any { it.id == item.id }

        if (isAlreadySaved) {
            _savedItems.value = currentSavedItems.filterNot { it.id == item.id }
            showSnackbar(R.string.snackbar_saved_removed)
        } else {
            _savedItems.value = listOf(item) + currentSavedItems
            showSnackbar(R.string.snackbar_saved_added)
        }

        savedMediaStorage.saveSavedMedia(_savedItems.value)
    }

    fun saveProfile(name: String, photoUri: String?) {
        val updatedProfile = UserProfile(
            name = name.ifBlank { DEFAULT_PROFILE_NAME },
            photoUri = photoUri
        )
        _profile.value = updatedProfile
        profileStorage.saveProfile(updatedProfile)
        showSnackbar(R.string.snackbar_profile_saved)
    }

    fun toggleWatched(itemId: Int) {
        val updatedWatchedIds = _watchedIds.value.toMutableSet()

        if (updatedWatchedIds.contains(itemId)) {
            updatedWatchedIds.remove(itemId)
        } else {
            updatedWatchedIds.add(itemId)
        }

        _watchedIds.value = updatedWatchedIds
        watchedStorage.saveWatchedIds(_watchedIds.value)
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

        val updatedReviews = _reviews.value.toMutableMap()
        updatedReviews[itemId] = review
        _reviews.value = updatedReviews
        reviewStorage.saveReviews(_reviews.value)
        showSnackbar(R.string.snackbar_review_saved)
    }

    fun deleteReview(itemId: Int) {
        val updatedReviews = _reviews.value.toMutableMap()
        updatedReviews.remove(itemId)
        _reviews.value = updatedReviews
        reviewStorage.saveReviews(_reviews.value)
        showSnackbar(R.string.snackbar_review_deleted)
    }

    fun selectMediaItem(item: TmdbMediaItem) {
        selectedMediaItem = item
    }

    fun getMediaItemById(itemId: Int): TmdbMediaItem? {
        if (selectedMediaItem?.id == itemId) {
            return selectedMediaItem
        }

        val savedItem = _savedItems.value.firstOrNull { it.id == itemId }
        if (savedItem != null) {
            return savedItem
        }

        val searchItem = _searchResults.value.firstOrNull { it.id == itemId }
        if (searchItem != null) {
            return searchItem
        }

        return _mediaItems.value.firstOrNull { it.id == itemId }
    }

    private fun createReviewDateTime(): String {
        val formatter = DateTimeFormatter.ofPattern(REVIEW_DATE_TIME_PATTERN)
        return LocalDateTime.now().format(formatter)
    }

    private fun showSnackbar(messageResId: Int) {
        _snackbarMessages.tryEmit(messageResId)
    }
}

private const val DEFAULT_PROFILE_NAME = "User"
private const val UNKNOWN_ERROR_MESSAGE = "Unknown error occurred"
private const val REVIEW_DATE_TIME_PATTERN = "dd-MM-yyyy HH:mm"
private const val SEARCH_DEBOUNCE_MS = 300L
