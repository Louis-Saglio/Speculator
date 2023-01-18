package com.speculator.tbm

import io.ktor.server.application.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.routing.*

fun Application.tbm() {
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
}