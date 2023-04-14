package com.speculator.tbm.vcub

import com.speculator.tbm.DefaultTemplate
import io.ktor.server.application.*
import io.ktor.server.html.*


suspend fun respondVcubStationsStatusAsHtml(call: ApplicationCall) {
    val stationNames = call.request.queryParameters.getAll("station") ?: emptyList()
    val stations = VcubDataFromFrontPageSerializer.fromFile().toVcubStationList()
    val requestedStations = stationNames.mapNotNull { stations.fuzzyGetStationByName(it) }
    call.respondHtmlTemplate(DefaultTemplate()) {
        renderVcubStationsAsHtml(requestedStations, emptyMap())
    }
}