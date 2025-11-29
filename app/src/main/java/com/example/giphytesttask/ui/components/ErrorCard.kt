package com.example.giphytesttask.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.giphytesttask.viewmodels.ErrorType

@Composable
fun ErrorCard(
    error: ErrorType,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = when (error) {
                is ErrorType.NoResults -> "No GIFs found for \"$searchQuery\""
                is ErrorType.Timeout -> "Request timed out. Please check your connection."
                is ErrorType.NetworkError -> "Network error. Please check your internet connection."
                is ErrorType.ServerError -> "Server error (${error.code}). Please try again later."
                is ErrorType.Unknown -> "Error: ${error.message}"
            },
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}