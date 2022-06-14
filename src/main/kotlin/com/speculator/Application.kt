package com.speculator

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(CIO, port = 8081, host = "127.0.0.1") {
        install(CallLogging)
        routing {
            get("/bus") {
                respondMyBusesNextPassageAsHtml(call)
            }
            get("/vcub") {
                respondMyVcubStationsStatusAsHtml(call)
            }
            post("/add-to-vcub-url") {
                addStationNameToVcubUrl(call)
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
}
