package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String
)