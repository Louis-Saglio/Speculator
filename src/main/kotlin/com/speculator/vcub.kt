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

suspend fun getMyVcubStationsStatusAsHtml(request: ApplicationRequest) = coroutineScope {
    val stationNames = request.queryParameters.getAll("station") ?: emptyList()
    val vcubStationsData = withContext(Dispatchers.IO) {
        client.get("https://carto.infotbm.com/api/realtime/data?display=bikes&data=vcub").body<VcubBikesCartoPayload>()
    }
    val vcubStations = stationNames.mapNotNull { stationName ->
        vcubStationsData.places.firstOrNull { p -> p.name == stationName }
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
                h2 { +it.name }
                h3 { +"Maintenant" }
                ul {
                    li { +"${it.stand.availablePlaces} places" }
                    li { +"${it.stand.availableBikes} vélos normaux" }
                    li { +"${it.stand.availableElectricBikes} vélos électriques" }
                }
                h3 { +"Predictions" }
                predictionsByStation[it]?.forEach { prediction ->
                    h4 { +"Dans ${prediction.minutesDelta} minutes" }
                    ol {
                        li { +"${prediction.availablePlaces} places" }
                        li { +"${prediction.availableBikes} vélos" }
                    }
                }
            }
        }
    }
}
