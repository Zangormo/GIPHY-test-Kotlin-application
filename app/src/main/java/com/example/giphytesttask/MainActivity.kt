package com.example.giphytesttask

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.Coil
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import com.example.giphytesttask.ui.screens.DetailScreen
import com.example.giphytesttask.ui.screens.SearchScreen
import com.example.giphytesttask.ui.theme.GIPHYTestTaskTheme
import com.example.giphytesttask.viewmodels.SearchViewModelFactory
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val apiKey = BuildConfig.GIPHY_API_KEY

        val imageLoader = ImageLoader.Builder(this)
            .components {
                add(ImageDecoderDecoder.Factory())
            }
            .build()
        Coil.setImageLoader(imageLoader)

        setContent {
            GIPHYTestTaskTheme {
                val navController = rememberNavController()
                val factory = remember { SearchViewModelFactory(apiKey) }

                NavHost(
                    navController = navController,
                    startDestination = "search"
                ) {
                    composable("search") {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            SearchScreen(
                                viewModel = viewModel(factory = factory),
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
                                viewModel = viewModel(factory = factory),
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