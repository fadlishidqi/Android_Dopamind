package com.example.new_dopamind.data.model

data class DoctorResponse(
    val statusCode: Int,
    val message: String,
    val data: List<Doctor>
)

data class Doctor(
    val doctor_id: Int,
    val type_id: Int,
    val image: String,
    val name: String,
    val price: String,
    val experience: String,
    val created_at: String,
    val updated_at: String
)