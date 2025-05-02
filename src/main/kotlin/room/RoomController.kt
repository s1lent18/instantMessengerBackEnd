package com.example.room

import com.example.data.MessageDataSource
import com.example.data.model.Message
import com.example.data.model.PrivateChat
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import kotlinx.serialization.json.Json
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
                receiverUsername = receiverUsername,
                timestamp = System.currentTimeMillis()
            )

            println("Attempting to save message to DB")
            messageDataSource.insertPrivateMessage(
                senderUsername = senderUsername,
                receiverUsername = receiverUsername,
                message = messageEntity
            )
            println("Message saved successfully")

            members[receiverUsername]?.socket?.send(Frame.Text(Json.encodeToString(messageEntity)))

        } catch (e: Exception) {
            println("Failed to save message: ${e.stackTraceToString()}")
            throw e
        }
    }

    suspend fun getAllMessages(username: String): List<PrivateChat> {
        return messageDataSource.getAllMessagesForUser(username)
    }

    suspend fun markMessageAsRead(messageId: String) {
        return messageDataSource.markMessageAsRead(messageId = messageId)
    }

    suspend fun getAllChatsForUser(username: String) : List<PrivateChat> {
        return messageDataSource.getAllChatsForUser(username = username)
    }

    suspend fun tryDisconnect(username: String) {
        members[username]?.socket?.close()
        members.remove(username)

    }
}