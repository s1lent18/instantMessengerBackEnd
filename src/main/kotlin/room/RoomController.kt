package com.example.room

import com.example.data.MessageDataSource
import com.example.data.model.Message
import com.example.data.model.PrivateChat
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import java.util.concurrent.ConcurrentHashMap

class RoomController(
    private val messageDataSource: MessageDataSource
) {
    private val members = ConcurrentHashMap<String, Member>()

    fun onJoin(username: String, sessionId: String, socket: WebSocketSession) {
        if (members.containsKey(username)) {
            throw MemberAlreadyExistsException()
        }
        members[username] = Member(username, sessionId, socket)
    }

    suspend fun sendMessage(senderUsername: String, receiverUsername: String, messageText: String) {
        println("RoomController.sendMessage() called with:")
        println("Sender: $senderUsername, Receiver: $receiverUsername, Text: $messageText")

        try {
            val messageEntity = Message(
                text = messageText,
                senderUsername = senderUsername,
                timestamp = System.currentTimeMillis()
            )

            println("Attempting to save message to DB")
            messageDataSource.insertPrivateMessage(
                senderUsername = senderUsername,
                receiverUsername = receiverUsername,
                message = messageEntity
            )
            println("Message saved successfully")

        } catch (e: Exception) {
            println("Failed to save message: ${e.stackTraceToString()}")
            throw e
        }
    }

    suspend fun getAllMessages(username: String): List<PrivateChat> {
        return messageDataSource.getAllMessagesForUser(username)
    }

    suspend fun tryDisconnect(username: String) {
        members[username]?.socket?.close()
        members.remove(username)
    }
}