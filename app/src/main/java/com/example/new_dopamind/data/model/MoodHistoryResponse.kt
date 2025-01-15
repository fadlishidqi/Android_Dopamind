package com.example.new_dopamind.data.model

data class MoodHistoryResponse(
    val statusCode: Int,
    val message: String,
    val data: List<MoodData>
)

data class MoodData(
    val mood_id: Int,
    val user_id: Int,
    val predictions: String,
    val texts: String,
    val created_at: String
)