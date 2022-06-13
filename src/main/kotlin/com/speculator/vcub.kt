package com.speculator

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.html.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.text.Normalizer

@Serializable
class VcubBikesCartoPayload(val places: List<VcubStation>)

@Serializable
class VcubStation(val name: String, @SerialName("id") val poiId: String, val stand: VcubStationStand) {
    @Transient val id = poiId.split(":").last().toInt()
}

@Serializable
class VcubStationStand(
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

fun simplifyString(string: String): String {
    return Normalizer.normalize(string.lowercase(), Normalizer.Form.NFKD).filter { it in "abcdefghijklmnopqrstuvwxyz" }
}

fun String.computeSimilarityScoreWith(b: String): Int {
    val thisSet = toSet()
    val bSet = b.toSet()
    var commonLettersNbr = 0
    thisSet.forEach {
        if (it in bSet) {
            commonLettersNbr += 1
        }
    }
    bSet.forEach {
        if (it !in thisSet) {
            commonLettersNbr -= 1
        }
    }
    return commonLettersNbr
}

suspend fun getMyVcubStationsStatusAsHtml(request: ApplicationRequest) = coroutineScope {
    val stationNames = request.queryParameters.getAll("station") ?: emptyList()
    val vcubStationsData = withContext(Dispatchers.IO) {
        client.get("https://carto.infotbm.com/api/realtime/data?display=bikes&data=vcub").body<VcubBikesCartoPayload>()
    }
    val vcubStations = stationNames.mapNotNull { stationName ->
        var bestSimilarityScore: Int? = null
        var bestMatch: VcubStation? = null
        for (p in vcubStationsData.places) {
            val requestedStationName = simplifyString(stationName)
            val similarityScore = requestedStationName.computeSimilarityScoreWith(simplifyString(p.name))
            if (bestSimilarityScore == null || similarityScore > bestSimilarityScore) {
                bestSimilarityScore = similarityScore
                bestMatch = p
            }
        }
        bestMatch
    }
    val predictionsByStation = vcubStations.map {
        it to async(Dispatchers.IO) {
            client.get("https://ws.infotbm.com/ws/1.0/vcubs/predict/15-30/${it.id}").body<VcubStationStatePredictionsPayload>()
        }
    }.associate { (station, deferredPrediction) -> station to deferredPrediction.await().predictions.data }
    renderTemplate(DefaultTemplate()) {
        tabTitle { +"Mes stations Vcub" }
        pageTitle { +"Mes stations Vcub" }
        content {
            vcubStations.forEach {
                section {
                    h2(classes = "section-title") { +it.name }
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
        }
    }
}
