package com.example.data.model

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Message(
    val text: String,
    val senderUsername: String,
    val receiverUsername: String,
    val timestamp: Long,
    val read: Boolean = false,
    @BsonId val id: String = ObjectId().toString()
)