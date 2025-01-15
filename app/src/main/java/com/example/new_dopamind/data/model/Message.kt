package com.example.new_dopamind.data.model

data class Message(
    val content: String,
    val isFromBot: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)