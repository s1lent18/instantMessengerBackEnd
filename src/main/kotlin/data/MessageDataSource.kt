package com.example.data

import com.example.data.model.Message
import com.example.data.model.PrivateChat

interface MessageDataSource {
    suspend fun getAllMessagesForUser(userId: String): List<PrivateChat>
    suspend fun insertPrivateMessage(senderUsername: String, receiverUsername: String, message: Message)
    suspend fun markMessageAsRead(messageId: String)
    suspend fun getAllChatsForUser(username: String): List<PrivateChat>
}