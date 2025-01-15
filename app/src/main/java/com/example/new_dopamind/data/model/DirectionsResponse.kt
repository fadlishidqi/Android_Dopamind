package com.example.new_dopamind.data.model

data class DirectionsResponse(
    val statusCode: Int,
    val message: String,
    val data: DirectionData
)

data class DirectionData(
    val hospital: Hospital,
    val distance: String,
    val duration: String,
    val steps: List<DirectionStep>,
    val polyline: String
)

data class Hospital(
    val name: String,
    val address: String,
    val location: Location
)

data class LocationPoint(
    val lat: Double,
    val lng: Double
)

data class DirectionStep(
    val instruction: String,
    val distance: String,
    val duration: String
)