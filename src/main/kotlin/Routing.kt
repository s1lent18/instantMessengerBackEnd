package com.example

import com.example.room.RoomController
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.Serializable
import org.slf4j.event.*
import com.example.routes.chatSocket
import com.example.routes.getAllMessages
import org.koin.ktor.ext.inject
import io.ktor.server.routing.*
import io.ktor.server.application.*

fun Application.configureRouting() {
    val roomController by inject<RoomController>()
    routing {
        chatSocket(roomController)
        getAllMessages(roomController)
    }
}
