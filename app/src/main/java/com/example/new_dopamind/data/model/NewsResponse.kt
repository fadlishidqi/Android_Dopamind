package com.example.new_dopamind.data.model

data class NewsResponse(
    val code: Int,
    val status: String,
    val messages: String,
    val total: Int,
    val data: List<NewsItem>
)

data class NewsItem(
    val title: String,
    val link: String,
    val contentSnippet: String,
    val isoDate: String,
    val image: NewsImage
)

data class NewsImage(
    val small: String,
    val large: String
)