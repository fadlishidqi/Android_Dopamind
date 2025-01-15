package com.example.new_dopamind.data.model

data class LoginResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val token: String
)

data class Data(
    val created_at: String,
    val email: String,
    val name: String,
    val phone: String,
    val updated_at: String,
    val user_id: Int,
    val username: String
)