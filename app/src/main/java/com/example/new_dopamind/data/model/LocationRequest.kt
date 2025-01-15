package com.example.new_dopamind.data.model

data class LocationRequest(
    val location: String,
    val radius: Int,
    val origin: String? = null
)