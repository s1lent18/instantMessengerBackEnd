package com.example.data

import com.example.data.model.Message
import com.example.data.model.PrivateChat
import com.example.data.model.User
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MessageDataSourceImpl(
    db: CoroutineDatabase
) : MessageDataSource {

    private val users = db.getCollection<User>("users")

    override suspend fun getAllMessagesForUser(userId: String): List<PrivateChat> {
        val user = users.findOne(User::username eq userId) ?: return emptyList()
        return user.chats
    }

    override suspend fun insertPrivateMessage(senderUsername: String, receiverUsername: String, message: Message) {
        println("\n==== Starting message insertion ====")
        println("Sender: $senderUsername, Receiver: $receiverUsername")
        println("Message content: $message")

        val sender = try {
            users.findOne(User::username eq senderUsername).also {
                println("Sender found: ${it != null}")
            } ?: run {
                println("ERROR: Sender not found!")
                return
            }
        } catch (e: Exception) {
            println("Sender lookup failed: ${e.message}")
            return
        }

        val receiver = try {
            users.findOne(User::username eq receiverUsername).also {
                println("Receiver found: ${it != null}")
            } ?: run {
                println("ERROR: Receiver not found!")
                return
            }
        } catch (e: Exception) {
            println("Receiver lookup failed: ${e.message}")
            return
        }

        println("Proceeding with IDs - Sender: ${sender.id}, Receiver: ${receiver.id}")

        // Update sender's chat
        val senderChat = sender.chats.find { it.otherUserId == receiver.id }?.also {
            println("Found existing chat for sender")
        } ?: run {
            println("Creating new chat for sender")
            PrivateChat(otherUserId = receiver.id)
        }

        val updatedSender = sender.copy(
            chats = sender.chats.filter { it.otherUserId != receiver.id } +
                    senderChat.copy(messages = senderChat.messages + message)
        )

        try {
            val updateResult = users.updateOneById(sender.id, updatedSender)
            println("Sender update result: ${updateResult.modifiedCount} documents modified")
        } catch (e: Exception) {
            println("Failed to update sender: ${e.message}")
        }

        // Similar update for receiver...
        // (Include the same detailed logging for receiver updates)
    }
}