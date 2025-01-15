package com.example.new_dopamind.data.api

import com.example.new_dopamind.data.model.NewsResponse
import retrofit2.Response
import retrofit2.http.GET

interface NewsApiService {
    @GET("v1/cnn-news/gaya-hidup")
    suspend fun getLifestyleNews(): Response<NewsResponse>
}