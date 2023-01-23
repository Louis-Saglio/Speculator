package com.speculator.tryphon

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.tryphon() {
    routing {
        route("tryphon") {
            get("playground-text") {
                val chars = ('a'..'z').toMutableSet().apply { add(' ') }
                call.respondText(
                    (0..50).map { chars.random() }.joinToString("")
                )
            }
        }
        singlePageApplication {
            applicationRoute = "tryphon"
            useResources = true
            filesPath = "tryphon"
            defaultPage = "main.html"
        }
    }
}