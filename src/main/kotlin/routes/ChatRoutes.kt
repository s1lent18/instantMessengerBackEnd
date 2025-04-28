package com.example.routes

import com.example.data.model.IncomingMessage
import com.example.data.model.LoginRequest
import com.example.data.model.User
import com.example.room.RoomController
import com.example.session.ChatSession
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import java.util.UUID
import org.litote.kmongo.reactivestreams.KMongo

fun Route.chatSocket(roomController: RoomController) {
    webSocket("/chat-socket") {
        val session = call.sessions.get<ChatSession>()
        println("WebSocket connected for user: ${session?.username}")

        try {
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    println("Received raw message: $text")

                    try {
                        val incomingMessage = Json.decodeFromString<IncomingMessage>(text)
                        println("Calling roomController.sendMessage()")

                        roomController.sendMessage(
                            senderUsername = session?.username ?: throw IllegalStateException("No session"),
                            receiverUsername = incomingMessage.to,
                            messageText = incomingMessage.text
                        ).also {
                            println("roomController.sendMessage() completed")
                        }

                    } catch (e: Exception) {
                        println("WebSocket message processing failed: ${e.stackTraceToString()}")
                    }
                }
            }
        } finally {
            println("WebSocket closed for ${session?.username}")
        }
    }
}

fun Route.getAllMessages(roomController: RoomController) {
    get("/messages") {
        val session = call.sessions.get<ChatSession>()
        if (session == null) {
            call.respond(HttpStatusCode.Unauthorized, "No session.")
            return@get
        }

        val messages = roomController.getAllMessages(session.username) // 👈 use username
        call.respond(HttpStatusCode.OK, messages)
    }
}

fun Route.loginRoute() {
    post("/login") {
        val loginRequest = call.receive<LoginRequest>()
        val username = loginRequest.username

        val db = KMongo.createClient().coroutine.getDatabase("InstantMessenger")
        val users = db.getCollection<User>("users")

        val existingUser = users.findOne(User::username eq username)

        if (existingUser == null) {
            val newUser = User(username = username)
            try {
                users.insertOne(newUser)
            } catch (_: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "User creation failed")
                return@post
            }
        }

        // Explicitly set the session after successful login
        call.sessions.set(ChatSession(
            username = username,
            sessionId = UUID.randomUUID().toString()
        ))

        call.respond(HttpStatusCode.OK, "Login successful")
    }
}