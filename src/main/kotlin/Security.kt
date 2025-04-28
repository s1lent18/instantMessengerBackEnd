package com.example

import com.example.session.ChatSession
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.sessions.*
import io.ktor.server.response.*

fun Application.configureSecurity() {
    install(Sessions) {
        cookie<ChatSession>("SESSION") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 3600
            cookie.httpOnly = true
            cookie.extensions["SameSite"] = "lax"
        }
    }

    install(Authentication) {
        session<ChatSession>("auth-session") {
            validate { session ->
                if (session.username.isNotEmpty()) {
                    session
                } else {
                    null
                }
            }
            challenge {
                call.respond(HttpStatusCode.Unauthorized, "Not authenticated")
            }
        }
    }
}
