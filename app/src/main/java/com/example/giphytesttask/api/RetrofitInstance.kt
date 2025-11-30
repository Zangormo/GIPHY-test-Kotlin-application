package com.example.giphytesttask.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "https://api.giphy.com/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // 'lazy' means this variable will be initialized only when it's first accessed.
    // This is a thread-safe way to create a Singleton in Kotlin.
    val api: GiphyApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Set the HTTP client (OkHttp) to handle requests
            .client(client)
            // Add a converter factory to parse JSON responses into Kotlin objects using Moshi
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            // Create an implementation of the GiphyApi interface
            .create(GiphyApi::class.java)
    }
}
