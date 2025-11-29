package com.example.giphytesttask.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class DetailViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val gifUrl: String = savedStateHandle["gifUrl"] ?: ""

    fun setGifUrl(url: String) {
        savedStateHandle["gifUrl"] = url
    }
}