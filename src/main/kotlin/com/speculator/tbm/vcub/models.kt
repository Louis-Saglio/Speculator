package com.speculator.tbm.vcub

import com.speculator.client
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class VcubStation(
    val name: String,
    val id: Int,
    val availableBikes: Int,
    val availableElectricBikes: Int,
    val availablePlaces: Int,
    val latitude: Double,
    val longitude: Double
)

data class VcubStationStatePrediction(
    val availablePlaces: Int,
    val availableBikes: Int,
    val minutesDelta: Int
)


fun List<VcubStation>.fuzzyGetStationByName(fuzzyStationName: String): VcubStation? {
    val simplifiedFuzzyStationName = fuzzyStationName.simplify()
    var bestScore = 0
    var bestStation: VcubStation? = null
    forEach {
        val score = it.name.simplify().computeSimilarityScoreWith(simplifiedFuzzyStationName)
        if (score > bestScore) {
            bestScore = score
            bestStation = it
        }
    }
    return bestStation
}

suspend fun List<VcubStation>.getPredictions() = coroutineScope {
    map {
        it to async(Dispatchers.IO) {
            client.get("https://ws.infotbm.com/ws/1.0/vcubs/predict/15-30/${it.id}")
                .body<VcubStationStatePredictionsSerializer>().toVcubStationStatePredictionList()
        }
    }.associate { (station, deferredPrediction) ->  station to deferredPrediction.await() }
}