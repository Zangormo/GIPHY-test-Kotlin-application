package api

data class GiphyResponse(
    val data: List<GiphyGif>
)

data class GiphyGif(
    val id: String,
    val title: String,
    val images: GiphyImages
)

data class GiphyImages(
    val original: GiphyOriginal
)

data class GiphyOriginal(
    val url: String
)
