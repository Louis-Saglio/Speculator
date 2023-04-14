package com.speculator.tbm.vcub

import com.speculator.client
import com.speculator.tbm.DefaultTemplate
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.html.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.pow
import kotlin.math.sqrt

@Serializable
class VcubBikesCartoPayloadFromAPI(val places: List<VcubStationFromAPI>)

@Serializable
class CoordinatesFromAPI(@SerialName("lat") val latitude: Double, @SerialName("lon") val longitude: Double)

@Serializable
class VcubStationFromAPI(
    val name: String,
    @SerialName("id") val poiId: String,
    val stand: VcubStationStandFomAPI,
    @SerialName("coord") val coordinates: CoordinatesFromAPI
) {
    @Transient
    val id = poiId.split(":").last().toInt()
}

@Serializable
class VcubStationStandFomAPI(
    @SerialName("available_bikes") val availableBikes: Int,
    @SerialName("available_electric_bikes") val availableElectricBikes: Int,
    @SerialName("available_places") val availablePlaces: Int,
)

@Serializable
class VcubStationStatePredictionsPayload(val predictions: VcubStationStatePredictionsData)

@Serializable
class VcubStationStatePredictionsData(val data: List<VcubStationStatePrediction>)

@Serializable
class VcubStationStatePrediction(
    @SerialName("free_slots") val availablePlaces: Int,
    @SerialName("bikes") val availableBikes: Int,
    @SerialName("tau") val minutesDelta: Int
)

@Serializable
class VcubStationFromFrontPage(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val nbBikeAvailable: Int,
    val nbPlaceAvailable: Int,
    val nbElectricBikeAvailable: Int,
    val connexionState: String
)

suspend fun respondMyVcubStationsStatusAsHtml(call: ApplicationCall) = coroutineScope {
    val stationNames = call.request.queryParameters.getAll("station") ?: emptyList()
    val vcubStationsData = withContext(Dispatchers.IO) {
        client.get("https://carto.infotbm.com/api/realtime/data?display=bikes&data=vcub")
            .body<VcubBikesCartoPayloadFromAPI>()
    }
    val vcubStations = stationNames.mapNotNull { stationName ->
        var bestSimilarityScore: Int? = null
        var bestMatch: VcubStationFromAPI? = null
        for (p in vcubStationsData.places) {
            val requestedStationName = stationName.simplify()
            val similarityScore = requestedStationName.computeSimilarityScoreWith(p.name.simplify())
            if (bestSimilarityScore == null || similarityScore > bestSimilarityScore) {
                bestSimilarityScore = similarityScore
                bestMatch = p
            }
        }
        bestMatch
    }
    val predictionsByStation = vcubStations.map {
        it to async(Dispatchers.IO) {
            client.get("https://ws.infotbm.com/ws/1.0/vcubs/predict/15-30/${it.id}")
                .body<VcubStationStatePredictionsPayload>()
        }
    }.associate { (station, deferredPrediction) -> station to deferredPrediction.await().predictions.data }
    call.respondHtmlTemplate(DefaultTemplate()) {
        tabTitle { +"Mes stations Vcub" }
        pageTitle { +"Mes stations Vcub" }
        content {
            vcubStations.forEach {
                section {
                    h2(classes = "section-title") {
                        a {
                            href =
                                "https://www.google.com/maps/search/?api=1&query=${it.coordinates.latitude},${it.coordinates.longitude}"
                            +it.name
                        }
                    }
                    div(classes = "section-content") {
                        div(classes = "sub-section") {
                            h3 { +"Maintenant" }
                            ul {
                                li { +"${it.stand.availablePlaces} places" }
                                li { +"${it.stand.availableBikes} vélos normaux" }
                                li { +"${it.stand.availableElectricBikes} vélos électriques" }
                            }
                        }
                        predictionsByStation[it]?.forEach { prediction ->
                            div(classes = "sub-section") {
                                h3 { +"Dans ${prediction.minutesDelta} minutes" }
                                ul {
                                    li { +"${prediction.availablePlaces} places" }
                                    li { +"${prediction.availableBikes} vélos" }
                                }
                            }
                        }
                    }
                }
            }
            section(classes = "add-station") {
                form {
                    action = "/vcub/add-to-url"
                    method = FormMethod.post
                    input(classes = "station-name-input") {
                        type = InputType.text
                        name = "station-name"
                    }
                    input {
                        type = InputType.submit
                        value = "Add"
                    }
                }
            }
        }
    }
}

suspend fun addStationNameToVcubUrl(call: ApplicationCall) {
    val newStationName = call.receiveParameters()["station-name"]
    val referer = call.request.headers["Referer"]
    if (referer == null) {
        call.respond(HttpStatusCode.BadRequest)
    } else {
        val urlBuilder = URLBuilder(referer)
        if (newStationName != null) {
            urlBuilder.parameters.append("station", newStationName)
        }
        call.respondRedirect(urlBuilder.buildString())
    }
}

suspend fun buildUrlForClosestStations(call: ApplicationCall) {
    val longitude = call.request.queryParameters["longitude"]?.toDoubleOrNull()
    val latitude = call.request.queryParameters["latitude"]?.toDoubleOrNull()
    if (latitude == null || longitude == null) {
        call.respond(HttpStatusCode.BadRequest)
        return
    }
    val vcubStationsData = withContext(Dispatchers.IO) {
        client.get("https://carto.infotbm.com/api/realtime/data?display=bikes&data=vcub")
            .body<VcubBikesCartoPayloadFromAPI>()
    }
    val closestStations = vcubStationsData.places.sortedBy {
        sqrt((longitude - it.coordinates.longitude).pow(2) + (latitude - it.coordinates.latitude).pow(2))
    }.subList(0, 3)
    call.respondRedirect(
        URLBuilder().apply {
            protocol = URLProtocol.byName[call.request.origin.scheme] ?: URLProtocol.HTTP
            port = call.request.port()
            host = call.request.host()
            path("/vcub")
            closestStations.forEach {
                parameters.append("station", it.name)
            }
        }.buildString()
    )
}

suspend fun respondGetCoordinatesScript(call: ApplicationCall) {
    call.respondHtml {
        head {
            script {
                unsafe {
                    //language=JavaScript
                    raw(
                        """
                        navigator.geolocation.getCurrentPosition(coordinates => {                        
                            window.location.href = window.location.origin + '/vcub/closest?latitude=' + coordinates.coords.latitude + '&longitude=' + coordinates.coords.longitude
                        })
                        """.trimIndent()
                    )
                }
            }
        }
    }
}
