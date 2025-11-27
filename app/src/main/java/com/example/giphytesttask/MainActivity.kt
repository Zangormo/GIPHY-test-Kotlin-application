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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.clickable
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.example.giphytesttask.ui.theme.GIPHYTestTaskTheme
import api.RetrofitInstance
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import coil.compose.SubcomposeAsyncImage
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val apiKey = "API_KEY"

        setContent {
            GIPHYTestTaskTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "search"
                ) {
                    composable("search") {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            SearchScreen(
                                apiKey = apiKey,
                                modifier = Modifier.padding(innerPadding),
                                onGifClick = { gifUrl ->
                                    val encodedUrl = URLEncoder.encode(gifUrl, StandardCharsets.UTF_8.toString())
                                    navController.navigate("detail/$encodedUrl")
                                }
                            )
                        }
                    }

                    composable(
                        route = "detail/{gifUrl}",
                        arguments = listOf(navArgument("gifUrl") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val encodedUrl = backStackEntry.arguments?.getString("gifUrl") ?: ""
                        val gifUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())

                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            DetailScreen(
                                gifUrl = gifUrl,
                                modifier = Modifier.padding(innerPadding),
                                onBackClick = { navController.navigateUp() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchScreen(
    apiKey: String,
    modifier: Modifier = Modifier,
    onGifClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("cats") }
    var gifs by remember { mutableStateOf(listOf<String>()) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var offset by remember { mutableStateOf(0) }
    var canLoadMore by remember { mutableStateOf(true) }
    val limit = 50
    val coroutineScope = rememberCoroutineScope()

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
        coroutineScope.launch {
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
        if (isLoading && gifs.isEmpty()) {
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
                SubcomposeAsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onGifClick(url) },
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .background(Color(0xFFE0E0E0))
                        )
                    }
                )

                // Load more when reaching near the end
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

@Composable
fun DetailScreen(
    gifUrl: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        // Centered GIF display
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = gifUrl,
                contentDescription = "Detailed GIF view",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth(),
                loading = {
                    CircularProgressIndicator()
                }
            )
        }
    }
}