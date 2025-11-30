package com.example.giphytesttask.api

import retrofit2.http.GET
import retrofit2.http.Query

interface GiphyApi {

    // 'suspend' keyword indicates that this function is asynchronous and can pause execution
    // without blocking the main thread. It must be called from a coroutine or another suspend function.
    @GET("v1/gifs/search")
    suspend fun searchGifs(
        @Query("api_key") apiKey: String,
        @Query("q") query: String,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): GiphyResponse
}