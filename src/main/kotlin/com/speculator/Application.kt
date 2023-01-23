package com.speculator

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.plugins() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
    faviconDispatcher()
}

fun Application.faviconDispatcher() {
    routing {
        get("favicon.ico") {
            val refererPath = this.call.request.headers["referer"]
            val path = if (refererPath == null) {
                ""
            } else {
                val referer = URLBuilder(refererPath)
                if (referer.pathSegments.size < 2) {
                    ""
                } else {
                    referer.pathSegments[1]
                }
            }
            call.respondRedirect {
                path("$path/favicon.ico")
            }
        }
    }
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
