package com.example.giphytesttask.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giphytesttask.api.RetrofitInstance
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

data class SearchUiState(
    val searchQuery: String = "",
    val gifs: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: ErrorType? = null,
    val offset: Int = 0,
    val canLoadMore: Boolean = true
)

sealed class ErrorType {
    object NoResults : ErrorType()
    object Timeout : ErrorType()
    object NetworkError : ErrorType()
    data class ServerError(val code: Int) : ErrorType()
    data class Unknown(val message: String) : ErrorType()
}

class SearchViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val apiKey: String
) : ViewModel() {

    // MutableStateFlow is a state holder observable flow that emits the current and new state updates.
    // It's like a variable that you can observe for changes.
    private val _uiState = MutableStateFlow(
        SearchUiState(
            searchQuery = savedStateHandle["searchQuery"] ?: "",
            gifs = savedStateHandle["gifs"] ?: emptyList(),
            offset = savedStateHandle["offset"] ?: 0
        )
    )
    // Expose an immutable StateFlow to the UI so it can't modify the state directly.
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private val limit = 50

    init {
        if (_uiState.value.gifs.isEmpty() && _uiState.value.searchQuery.isNotBlank()) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)
                performSearch(_uiState.value.searchQuery, 0, resetList = true)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        savedStateHandle["searchQuery"] = query

        // Cancel the previous search job if the user keeps typing.
        // This prevents unnecessary network requests for every keystroke.
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                gifs = emptyList(),
                offset = 0,
                canLoadMore = true,
                error = null
            )
            savedStateHandle["gifs"] = emptyList<String>()
            savedStateHandle["offset"] = 0
            return
        }

        // Launch a new coroutine in the viewModelScope.
        // viewModelScope is tied to the ViewModel's lifecycle and will be cancelled when ViewModel is cleared.
        searchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                offset = 0,
                canLoadMore = true
            )

            delay(1500) // Debounce: Wait for 1.5 seconds of inactivity before searching

            performSearch(query, 0, resetList = true)
        }
    }

    fun loadMoreGifs() {
        val currentState = _uiState.value
        if (currentState.isLoading || !currentState.canLoadMore || currentState.searchQuery.isBlank()) {
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true)
            performSearch(currentState.searchQuery, currentState.offset, resetList = false)
        }
    }

    private suspend fun performSearch(query: String, offset: Int, resetList: Boolean) {
        try {
            val response = RetrofitInstance.api.searchGifs(
                apiKey = apiKey,
                query = query,
                limit = limit,
                offset = offset
            )

            val newGifs = response.data.map { it.images.original.url }

            if (resetList && newGifs.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    gifs = emptyList(),
                    isLoading = false,
                    error = ErrorType.NoResults,
                    canLoadMore = false
                )
                savedStateHandle["gifs"] = emptyList<String>()
            } else {
                val updatedGifs = if (resetList) newGifs else _uiState.value.gifs + newGifs
                val newOffset = offset + limit

                _uiState.value = _uiState.value.copy(
                    gifs = updatedGifs,
                    isLoading = false,
                    error = null,
                    offset = newOffset,
                    canLoadMore = newGifs.size >= limit
                )

                savedStateHandle["gifs"] = updatedGifs
                savedStateHandle["offset"] = newOffset
            }
        } catch (e: Exception) {
            val errorType = when (e) {
                is SocketTimeoutException -> ErrorType.Timeout
                is UnknownHostException -> ErrorType.NetworkError
                else -> {
                    // Check for HTTP errors
                    val message = e.message ?: ""
                    when {
                        message.contains("500") -> ErrorType.ServerError(500)
                        message.contains("503") -> ErrorType.ServerError(503)
                        message.contains("429") -> ErrorType.ServerError(429)
                        else -> ErrorType.Unknown(message)
                    }
                }
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = errorType,
                gifs = if (resetList) emptyList() else _uiState.value.gifs
            )

            if (resetList) {
                savedStateHandle["gifs"] = emptyList<String>()
            }
        }
    }
}