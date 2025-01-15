package com.example.new_dopamind.data.model

data class RegisterResponse(
    val data: RegisterData,
    val message: String,
    val statusCode: Int,
    val token: String
)

data class RegisterData(
    val user_id: Int,
    val email: String,
    val username: String,
    val name: String,
    val phone: String,
    val created_at: String,
    val updated_at: String
)