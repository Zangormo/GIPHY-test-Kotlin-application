package com.example.giphytesttask.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.giphytesttask.ui.components.ErrorCard
import com.example.giphytesttask.ui.components.GifItem
import com.example.giphytesttask.viewmodels.SearchViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    modifier: Modifier = Modifier,
    onGifClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val columnCount = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 3 else 2

    Column(modifier = modifier) {
        TextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            label = { Text("Search GIFs") },
            placeholder = { Text("Enter keywords...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = true
        )

        if (uiState.isLoading && uiState.gifs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.error?.let { error ->
            ErrorCard(
                error = error,
                searchQuery = uiState.searchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(columnCount),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp,
            contentPadding = PaddingValues(8.dp)
        ) {
            itemsIndexed(uiState.gifs) { index, url ->
                GifItem(
                    url = url,
                    onClick = { onGifClick(url) }
                )

                if (index >= uiState.gifs.size - 5 && uiState.canLoadMore && !uiState.isLoading) {
                    LaunchedEffect(Unit) {
                        viewModel.loadMoreGifs()
                    }
                }
            }

            if (uiState.isLoading && uiState.gifs.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}