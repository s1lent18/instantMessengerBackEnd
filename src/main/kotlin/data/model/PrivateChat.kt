package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PrivateChat(
    val otherUserId: String,
    val messages: List<Message> = emptyList()
)
