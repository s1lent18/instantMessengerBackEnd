package com.example.data

import com.example.data.model.Message
import com.example.data.model.PrivateChat
import com.example.data.model.User
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.`in`
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates.set
import org.bson.types.ObjectId
import org.litote.kmongo.and
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

        val senderChat = sender.chats.find { it.otherUserId == receiver.id }?.also {
            println("Found existing chat for sender")
        } ?: run {
            println("Creating new chat for sender")
            PrivateChat(
                otherUserId = receiver.id,
            )
        }

        val updatedSender = sender.copy(
            chats = sender.chats.filter { it.otherUserId != receiver.id } +
                    senderChat.copy(messages = senderChat.messages + message)
        )

        val receiverChat = receiver.chats.find { it.otherUserId == sender.id }
            ?: PrivateChat(
                otherUserId = sender.id,
            )

        val updatedReceiver = receiver.copy(
            chats = receiver.chats.filter { it.otherUserId != sender.id } +
                    receiverChat.copy(messages = receiverChat.messages + message)
        )

        users.updateOneById(receiver.id, updatedReceiver)

        try {
            val updateResult = users.updateOneById(sender.id, updatedSender)
            println("Sender update result: ${updateResult.modifiedCount} documents modified")
        } catch (e: Exception) {
            println("Failed to update sender: ${e.message}")
        }
    }

    override suspend fun markMessageAsRead(messageId: String) {
        val filter = and(
            `in`("chats.messages._id", ObjectId(messageId))
        )

        val update = set("chats.$[chat].messages.$[message].read", true)

        users.updateMany(
            filter = filter,
            update = update,
            options = UpdateOptions().arrayFilters(
                listOf(
                    eq("chat.messages._id", ObjectId(messageId))
                )
            )
        )
    }

    override suspend fun getAllChatsForUser(username: String): List<PrivateChat> {
        return users.findOne(User::username eq username)?.chats ?: emptyList()
    }
}