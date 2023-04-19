package com.speculator.tbm.vcub

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
class VcubStationFromFrontPageSerializer(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val nbBikeAvailable: Int,
    val nbPlaceAvailable: Int,
    val nbElectricBikeAvailable: Int,
    val connexionState: String
)

@Serializable
class VcubDataFromFrontPageSerializer(val stations: List<VcubStationFromFrontPageSerializer>) {
    companion object {
        fun fromFile() =
            Json {
                ignoreUnknownKeys = true
            }.decodeFromString<VcubDataFromFrontPageSerializer>(File("src/main/resources/vcub.json").readText())
    }

    fun toVcubStationList(): List<VcubStation> {
        return stations.map {
            VcubStation(
                it.name,
                it.id,
                it.nbBikeAvailable,
                it.nbElectricBikeAvailable,
                it.nbPlaceAvailable,
                it.latitude,
                it.longitude
            )
        }
    }
}

@Serializable
class VcubStationStatePredictionsSerializer(val predictions: VcubStationStatePredictionsDataSerializer) {
    fun toVcubStationStatePredictionList() = predictions.data.map {
        VcubStationStatePrediction(it.availablePlaces, it.availableBikes, it.minutesDelta)
    }
}

@Serializable
class VcubStationStatePredictionsDataSerializer(val data: List<VcubStationStatePredictionSerializer>)

@Serializable
class VcubStationStatePredictionSerializer(
    @SerialName("free_slots") val availablePlaces: Int,
    @SerialName("bikes") val availableBikes: Int,
    @SerialName("tau") val minutesDelta: Int
)