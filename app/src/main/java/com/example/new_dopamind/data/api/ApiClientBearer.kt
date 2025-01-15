package com.example.new_dopamind.data.api
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClientBearer {
    companion object {
        fun create(token: String): ApiService {
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                    chain.proceed(request)
                }
                .build()

            val apiClient = Retrofit.Builder()
                .baseUrl("http://34.101.128.115:9000/api/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return apiClient.create(ApiService::class.java)
        }
    }
}