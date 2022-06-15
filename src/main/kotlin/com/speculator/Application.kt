package com.speculator

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(CIO, port = 8081, host = "127.0.0.1") {
        install(CallLogging)
        install(XForwardedHeaders)
        routing {
            get("/bus") {
                respondMyBusesNextPassageAsHtml(call)
            }
            route("vcub") {
                get {
                    respondMyVcubStationsStatusAsHtml(call)
                }
                get("closest") {
                    buildUrlForClosestStations(call)
                }
                post("add-to-url") {
                    addStationNameToVcubUrl(call)
                }
                get("closest-from-me") {
                    respondGetCoordinatesScript(call)
                }
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
