package com.example

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.html.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames

fun main() {
    embeddedServer(CIO, port = 8081, host = "0.0.0.0") {
        install(ShutDownUrl.ApplicationCallPlugin) {
            shutDownUrl = "/shutdown/"
        }
        configureBusStopsEndpoint()
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

@Serializable
data class BusArrival(@SerialName("waittime_text") val waitTimeText: String)

@Serializable
data class BusArrivalsResponsePayload0(@JsonNames("5315", "3647", "2947", "3270") val destinations: List<BusArrival>)

@Serializable
data class BusArrivalsResponsePayload(val destinations: BusArrivalsResponsePayload0)

data class BusArrivalMetaData(val stopName: String, val line: String, val urlPath: String)

val busStops = listOf(
    BusArrivalMetaData("Eglise de Caudéran", "2", "get-realtime-pass/3169/02"),
    BusArrivalMetaData("Eglise de Caudéran", "3", "get-realtime-pass/3169/03"),
    BusArrivalMetaData("Quinconces", "2", "get-realtime-pass/3648/02"),
    BusArrivalMetaData("Quinconces", "3", "get-realtime-pass/3648/03"),
)

suspend fun getNextBusArrival(data: BusArrivalMetaData) = withContext(Dispatchers.IO) {
    client.get(data.urlPath).body<BusArrivalsResponsePayload>().destinations.destinations
}

fun Application.configureBusStopsEndpoint() {
    routing {
        get("/bus") {
            val result = async {
                busStops.associateWith { getNextBusArrival(it) }
            }
            val nextBusArrivalsByStation = result.await()
            call.respondHtml {
                head {
                    title {
                        +"TBM - Bus"
                    }
                }
                body {
                    h1 {
                        +"Prochains bus"
                    }
                    nextBusArrivalsByStation.forEach { (station, nextBusArrivals) ->
                        h2 {
                            +"${station.stopName} - ${station.line}"
                        }
                        ol {
                            nextBusArrivals.forEach {
                                li { +it.waitTimeText }
                            }
                        }
                    }
                }
            }
        }
    }
}
