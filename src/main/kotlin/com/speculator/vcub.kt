package com.speculator

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.server.html.HtmlContent
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.html.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class VcubBikesCartoPayload(val places: List<VcubStation>)

@Serializable
class VcubStation(val name: String, val stand: VcubStationStand)

@Serializable
class VcubStationStand(
    @SerialName("available_bikes") val availableBikes: Int,
    @SerialName("available_electric_bikes") val availableElectricBikes: Int,
    @SerialName("available_places") val availablePlaces: Int,
)

suspend fun getMyVcubStationsStatusAsHtml(request: ApplicationRequest) = coroutineScope {
    val metadata = request.queryParameters.getAll("station") ?: emptyList()
    val vcubStationsData = withContext(Dispatchers.IO) {
        client.get("https://carto.infotbm.com/api/realtime/data?display=bikes&data=vcub").body<VcubBikesCartoPayload>()
    }
    val vcubStations = metadata.mapNotNull { stationName ->
        vcubStationsData.places.firstOrNull { p -> p.name == stationName }
    }
    HtmlContent {
        head {
            title {
                +"Mes stations Vcub"
            }
            script(type = ScriptType.textJavaScript) {
                unsafe {
                    //language=JavaScript
                    raw("window.setInterval(() => this.location.reload(), 20_000)")
                }
            }
        }
        body {
            h1 { +"Etats de mes stations Vcub" }
            vcubStations.forEach {
                h2 { +it.name }
                ul {
                    li { +"${it.stand.availablePlaces} places" }
                    li { +"${it.stand.availableBikes} vélos normaux" }
                    li { +"${it.stand.availableElectricBikes} vélos électriques" }
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
