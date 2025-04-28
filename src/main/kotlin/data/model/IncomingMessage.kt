package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class IncomingMessage(
    val to: String,
    val text: String
)