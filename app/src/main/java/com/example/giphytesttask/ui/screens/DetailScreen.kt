package com.example.giphytesttask.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.giphytesttask.viewmodels.DetailViewModel

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    gifUrl: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    LaunchedEffect(gifUrl) {
        viewModel.setGifUrl(gifUrl)
    }

    Column(modifier = modifier.fillMaxSize()) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = viewModel.gifUrl,
                contentDescription = "Detailed GIF view",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth(),
                loading = {
                    CircularProgressIndicator()
                },
                error = {
                    Text(
                        "Failed to load GIF",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}