package com.pafoid.kpoker

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlin.time.Duration.Companion.seconds

import com.pafoid.kpoker.network.PokerServer
import com.pafoid.kpoker.network.DatabaseFactory

fun main() {
    println("KPoker Server is starting...")
    DatabaseFactory.init()
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

val pokerServer = PokerServer()

fun Application.module() {
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        get("/") {
            call.respondText("KPoker Server is running. Ktor: ${Greeting().greet()}")
        }
        webSocket("/ws") {
            pokerServer.handleConnection(this)
        }
    }
}