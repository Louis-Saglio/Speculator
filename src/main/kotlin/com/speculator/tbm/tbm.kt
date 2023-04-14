package com.speculator.tbm

import com.speculator.tbm.vcub.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

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
            get("test") {
                // print current working directory
                println("Current working directory: ${System.getProperty("user.dir")}")
                // open the data file
                val data = File("src/main/resources/data").readText()
                val vcubData = data.split("\"vcubs\":").last().split("}},\"page\":").first()
                println(vcubData)
                // write vcub data into a json file with 4 space indents
                File("src/main/resources/vcub.json").writeText(vcubData)
                // format json file

                call.respond(HttpStatusCode.OK)
            }
        }
        route("v2") {
            route("vcub") {
                get {
                    respondVcubStationsStatusAsHtml(call)
                }
            }
        }
    }
}