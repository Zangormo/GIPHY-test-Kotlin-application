package com.example.giphytesttask.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.example.giphytesttask.api.GiphyApi
import com.example.giphytesttask.api.GiphyGif
import com.example.giphytesttask.api.GiphyImages
import com.example.giphytesttask.api.GiphyOriginal
import com.example.giphytesttask.api.GiphyResponse
import com.example.giphytesttask.api.RetrofitInstance
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    // 1. Create a test dispatcher to control time (skip delays)
    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var viewModel: SearchViewModel
    private val api = mockk<GiphyApi>()

    @Before
    fun setup() {
        // Set the Main thread to use our test dispatcher
        Dispatchers.setMain(testDispatcher)
        
        // Mock the API so we don't use real internet
        mockkObject(RetrofitInstance)
        coEvery { RetrofitInstance.api } returns api

        viewModel = SearchViewModel(SavedStateHandle(), "test_key")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `searching for cats returns results`() = runTest(testDispatcher) {
        // Given: The API returns a list with one cat
        val fakeResponse = GiphyResponse(
            data = listOf(
                GiphyGif("1", "Funny Cat", GiphyImages(GiphyOriginal("http://cat.gif")))
            )
        )
        coEvery { api.searchGifs(any(), any(), any(), any()) } returns fakeResponse

        // When: We search for "cats"
        viewModel.onSearchQueryChange("cats")
        
        // Wait for the 1.5 second delay (happens instantly in test)
        advanceUntilIdle()

        // Then: The list should contain 1 item
        assertEquals(1, viewModel.uiState.value.gifs.size)
        assertEquals("http://cat.gif", viewModel.uiState.value.gifs[0])
    }

    @Test
    fun `searching for empty string clears results`() = runTest(testDispatcher) {
        // Given: We search for "cats" first to get some data
        val fakeResponse = GiphyResponse(
            data = listOf(GiphyGif("1", "Cat", GiphyImages(GiphyOriginal("url"))))
        )
        coEvery { api.searchGifs(any(), any(), any(), any()) } returns fakeResponse
        viewModel.onSearchQueryChange("cats")
        advanceUntilIdle()

        // When: We change the query to an empty string
        viewModel.onSearchQueryChange("")
        
        // Then: The list should be empty immediately
        assertEquals(0, viewModel.uiState.value.gifs.size)
    }

    @Test
    fun `searching with no results shows empty list`() = runTest(testDispatcher) {
        // Given: The API returns an empty list (no matches found)
        val emptyResponse = GiphyResponse(data = emptyList())
        coEvery { api.searchGifs(any(), any(), any(), any()) } returns emptyResponse

        // When: We search for "weird_query"
        viewModel.onSearchQueryChange("weird_query")
        advanceUntilIdle()

        // Then: The list should be empty
        assertEquals(0, viewModel.uiState.value.gifs.size)
    }
}
