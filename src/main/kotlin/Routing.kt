package com.example

import com.example.room.RoomController
import com.example.routes.chatHistoryRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*
import com.example.routes.chatSocket
import com.example.routes.getAllMessages
import com.example.routes.loginRoute
import com.example.routes.messageReadRoutes
import com.example.routes.userRoutes
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val roomController by inject<RoomController>()
    routing {
        chatHistoryRoutes(roomController)
        messageReadRoutes(roomController)
        userRoutes()
        loginRoute()
        chatSocket(roomController)
        getAllMessages(roomController)
    }
}
