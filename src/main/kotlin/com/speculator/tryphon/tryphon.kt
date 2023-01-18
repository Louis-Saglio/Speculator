package com.speculator.tryphon

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

fun Application.tryphon() {
    routing {
        singlePageApplication {
            applicationRoute = "tryphon"
            useResources = true
            filesPath = "tryphon"
            defaultPage = "main.html"
        }
    }
}