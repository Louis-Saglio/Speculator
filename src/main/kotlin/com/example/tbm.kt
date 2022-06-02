package com.example

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.html.HtmlContent
import kotlinx.coroutines.*
import kotlinx.html.*
import kotlinx.serialization.json.*

private val JsonElement?.jsonObjectOrNull: JsonObject?
    get() = try {
        this?.jsonObject
    } catch (e: java.lang.IllegalArgumentException) {
        null
    }

class Arrival(val waitTime: Int, val waitTimeText: String, val lineName: String) {
    override fun toString(): String {
        return "$lineName : $waitTimeText"
    }
}

suspend fun getNextBusArrival(urlPath: String) = withContext(Dispatchers.IO) {
    val response = client.get(urlPath) // .body<BusArrivalsResponsePayload>().destinations.destinations
    val json = Json.parseToJsonElement(response.bodyAsText())
    json.jsonObject["destinations"]?.jsonObjectOrNull?.entries?.first()?.value?.jsonArray?.mapNotNull {
        val waitTime = it.jsonObject["waittime"]?.toString()?.replace("\"", "")
        val waitTimeText = it.jsonObject["waittime_text"]?.toString()?.replace("\"", "")
        if (waitTime != null && waitTimeText != null) {
            val (hours, minutes, seconds) = waitTime.split(":")
            Arrival(
                hours.toInt() * 3600 + minutes.toInt() * 60 + seconds.toInt(),
                waitTimeText,
                "Liane ${urlPath.split("/").last()}",
            )
        } else {
            null
        }
    } ?: emptyList()
}

suspend fun getMyBusesNextPassageAsHtml() = coroutineScope {
    val metaData = mapOf(
        "Eglise de caudéran" to listOf("get-realtime-pass/3169/02", "get-realtime-pass/3169/03"),
        "Quinconces" to listOf("get-realtime-pass/3648/02", "get-realtime-pass/3648/03")
    )
    val data = metaData
        .map { (name, urlPaths) -> name to urlPaths.map { async { getNextBusArrival(it) } } }
        .map { (name, deferredWaitTimes) ->
            name to deferredWaitTimes.awaitAll().flatten().sortedBy { it.waitTime }
        }
    HtmlContent {
        head {
            title {
                +"Mes prochains bus"
            }
            script(type = ScriptType.textJavaScript) {
                unsafe {
                    //language=JavaScript
                    raw("window.setInterval(() => this.location.reload(), 20_000)")
                }
            }
        }
        body {
            h1 {
                +"Prochains bus"
            }
            data.forEach { (name, waitTimes) ->
                h2 {
                    +name
                }
                ul {
                    waitTimes.forEach {
                        li {
                            +it.toString()
                        }
                    }
                }
            }
            button {
                //language=JavaScript
                onClick = "location.reload()"
                +"Refresh"
            }
        }
    }
}
