package com.example

import com.example.room.RoomController
import io.ktor.server.application.*
import io.ktor.server.routing.*
import com.example.routes.chatSocket
import com.example.routes.getAllMessages
import com.example.routes.loginRoute
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val roomController by inject<RoomController>()
    routing {
        loginRoute()
        chatSocket(roomController)
        getAllMessages(roomController)
    }
}
