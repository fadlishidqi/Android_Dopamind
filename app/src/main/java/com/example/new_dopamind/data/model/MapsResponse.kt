package com.example.new_dopamind.data.model

data class MapsResponse(
    val statusCode: Int,
    val message: String,
    val data: List<HospitalData>
)

data class HospitalData(
    val name: String,
    val address: String,
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)