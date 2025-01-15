package com.example.new_dopamind.data.api

import com.example.new_dopamind.data.model.ChatMessage
import com.example.new_dopamind.data.model.ChatResponse
import com.example.new_dopamind.data.model.DirectionsResponse
import com.example.new_dopamind.data.model.DoctorResponse
import com.example.new_dopamind.data.model.LocationRequest
import com.example.new_dopamind.data.model.LoginBody
import com.example.new_dopamind.data.model.LoginResponse
import com.example.new_dopamind.data.model.MapsResponse
import com.example.new_dopamind.data.model.MoodHistoryResponse
import com.example.new_dopamind.data.model.NewsResponse
import com.example.new_dopamind.data.model.PredictRequest
import com.example.new_dopamind.data.model.PredictResponse
import com.example.new_dopamind.data.model.RegisterBody
import com.example.new_dopamind.data.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun userLogin(@Body loginBody: LoginBody): Response<LoginResponse>

    @POST("auth/register")
    suspend fun userRegister(@Body registerBody: RegisterBody): Response<RegisterResponse>

    @POST("predict")
    suspend fun predictText(@Body predictRequest: PredictRequest): Response<PredictResponse>

    @GET("mood")
    suspend fun getMoodHistory(): Response<MoodHistoryResponse>

    @POST("chat")
    suspend fun sendMessage(@Body message: ChatMessage): Response<ChatResponse>

    @POST("maps")
    suspend fun getNearbyHospitals(@Body locationRequest: LocationRequest): Response<MapsResponse>

    @GET("doctor")
    suspend fun getDoctors(): Response<DoctorResponse>

    @POST("maps/directions")
    suspend fun getDirections(@Body locationRequest: LocationRequest): Response<DirectionsResponse>
}