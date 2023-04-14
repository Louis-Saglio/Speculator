package com.speculator.tbm.vcub

import com.speculator.tbm.DefaultTemplate
import kotlinx.html.*


fun DefaultTemplate.renderVcubStationsAsHtml(vcubStations: List<VcubStation>, predictionsByStation: Map<VcubStation, List<VcubStationPrediction>>) {
        tabTitle { +"Mes stations Vcub" }
        pageTitle { +"Mes stations Vcub" }
        content {
            vcubStations.forEach {
                section {
                    h2(classes = "section-title") {
                        a {
                            href =
                                "https://www.google.com/maps/search/?api=1&query=${it.latitude},${it.longitude}"
                            +it.name
                        }
                    }
                    div(classes = "section-content") {
                        div(classes = "sub-section") {
                            h3 { +"Maintenant" }
                            ul {
                                li { +"${it.availablePlaces} places" }
                                li { +"${it.availableBikes} vélos normaux" }
                                li { +"${it.availableElectricBikes} vélos électriques" }
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