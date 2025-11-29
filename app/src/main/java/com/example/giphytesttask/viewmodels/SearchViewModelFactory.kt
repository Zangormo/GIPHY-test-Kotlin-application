package com.example.giphytesttask.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle

class SearchViewModelFactory(
    private val apiKey: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: androidx.lifecycle.viewmodel.CreationExtras
    ): T {
        val savedStateHandle = extras.createSavedStateHandle()

        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel(savedStateHandle, apiKey) as T
            }
            modelClass.isAssignableFrom(DetailViewModel::class.java) -> {
                DetailViewModel(savedStateHandle) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}