package com.example

import com.example.plugins.configureRouting
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main() {
    embeddedServer(CIO, port = 8081, host = "0.0.0.0") {
        configureRouting()
        install(ShutDownUrl.ApplicationCallPlugin) {
            shutDownUrl = "/shutdown/"
        }
    }.start(wait = true)
}
