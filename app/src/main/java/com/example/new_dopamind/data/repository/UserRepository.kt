package com.example.new_dopamind.data.repository

import com.example.new_dopamind.data.api.ApiService
import com.example.new_dopamind.data.model.LoginBody
import com.example.new_dopamind.data.model.LoginResponse
import com.example.new_dopamind.data.model.PredictRequest
import com.example.new_dopamind.data.model.PredictResponse
import com.example.new_dopamind.data.model.RegisterBody
import com.example.new_dopamind.data.model.RegisterResponse
import retrofit2.Response

class UserRepository(private val apiService: ApiService) {
    suspend fun userLogin(loginBody: LoginBody): Response<LoginResponse> {
        return apiService.userLogin(loginBody)
    }

    suspend fun userRegister(registerBody: RegisterBody): Response<RegisterResponse> {
        return apiService.userRegister(registerBody)
    }

    suspend fun predictText(predictRequest: PredictRequest): Response<PredictResponse> {
        return apiService.predictText(predictRequest)
    }

}