package com.speculator

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(CIO, port = 8081, host = "127.0.0.1") {
        install(ShutDownUrl.ApplicationCallPlugin) {
            // todo : doesn't work
            shutDownUrl = "/shutdown/"
        }
        install(CallLogging)
        routing {
            get("/bus") {
                call.respond(getMyBusesNextPassageAsHtml())
            }
            get("/vcub") {
                call.respond(getMyVcubStationsStatusAsHtml())
            }
        }
    }.start(wait = true)
}

val client = HttpClient {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            },
            ContentType.Application.Json,
        )
    }
    defaultRequest {
        url {
            protocol = URLProtocol.HTTPS
            host = "ws.infotbm.com"
            url("/ws/1.0/")
        }
    }
}