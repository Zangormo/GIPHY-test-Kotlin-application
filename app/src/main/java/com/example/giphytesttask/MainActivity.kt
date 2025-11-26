package com.example.giphytesttask

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.example.giphytesttask.ui.theme.GIPHYTestTaskTheme
import api.RetrofitInstance
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.items
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val apiKey = "API_KEY"

        setContent {
            GIPHYTestTaskTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GifScreen(apiKey = apiKey, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun GifScreen(apiKey: String, modifier: Modifier = Modifier) {
    var searchQuery by remember { mutableStateOf("cats") }
    var gifs by remember { mutableStateOf(listOf<String>()) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var offset by remember { mutableStateOf(0) }
    var canLoadMore by remember { mutableStateOf(true) }
    val limit = 50 // Load 50 GIFs at a time (approximately 2 screens)

    // Debounced search effect - resets pagination
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            gifs = emptyList()
            offset = 0
            canLoadMore = true
            return@LaunchedEffect
        }

        isLoading = true
        error = null
        offset = 0
        canLoadMore = true

        // Wait for 1.5 seconds after user stops typing
        delay(1500)

        try {
            val response = RetrofitInstance.api.searchGifs(
                apiKey = apiKey,
                query = searchQuery,
                limit = limit,
                offset = 0
            )
            gifs = response.data.map { it.images.original.url }
            error = null
            offset = limit
            canLoadMore = response.data.size >= limit
        } catch (e: Exception) {
            error = e.message
            gifs = emptyList()
        } finally {
            isLoading = false
        }
    }

    // Function to load more GIFs
    fun loadMoreGifs() {
        if (isLoading || !canLoadMore || searchQuery.isBlank()) return

        isLoading = true
        kotlinx.coroutines.MainScope().launch {
            try {
                val response = RetrofitInstance.api.searchGifs(
                    apiKey = apiKey,
                    query = searchQuery,
                    limit = limit,
                    offset = offset
                )
                gifs = gifs + response.data.map { it.images.original.url }
                offset += limit
                canLoadMore = response.data.size >= limit
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    Column(modifier = modifier) {
        // Search input field
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search GIFs") },
            placeholder = { Text("Enter keywords...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = true
        )

        // Loading indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
        }

        // Error message
        if (error != null) {
            Text(
                "Error: $error",
                modifier = Modifier.padding(16.dp)
            )
        }

        // GIF grid with staggered layout
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)
        ) {
            itemsIndexed(gifs) { index, url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                )

                // Load more when reaching near the end (5 items before the end)
                if (index >= gifs.size - 5 && canLoadMore && !isLoading) {
                    LaunchedEffect(Unit) {
                        loadMoreGifs()
                    }
                }
            }

            // Show loading indicator at the bottom when loading more
            if (isLoading && gifs.isNotEmpty()) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}