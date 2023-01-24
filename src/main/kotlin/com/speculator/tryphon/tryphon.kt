package com.speculator.tryphon

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.util.NoSuchElementException

fun Application.tryphon() {
    routing {
        route("tryphon") {
            get("playground-text") {
                val chars = ('a'..'z').toMutableSet().apply { add(' ') }
                call.respondText(
                    (0..50).map { chars.random() }.joinToString("")
                )
            }
            get("generate-text") {
                val frequency = computeFrequency(loadText())
                println(frequency)
                val text = generateText(frequency, 50)
                call.respondText(text)
            }
        }
        singlePageApplication {
            applicationRoute = "tryphon"
            useResources = true
            filesPath = "tryphon"
            defaultPage = "main.html"
        }
    }
}

typealias Frequency = Map<Pair<Char, Char>, Map<Char, Int>>

fun computeFrequency(text: String): Frequency {
    val result: MutableMap<Pair<Char, Char>, MutableMap<Char, Int>> = mutableMapOf()
    var prefix = Pair(text[0], text[1])
    for (char in text.slice(2 until text.length)) {
        val prefixMap = result.getOrPut(prefix, ::mutableMapOf)
        prefixMap[char] = prefixMap.getOrDefault(char, 0) + 1
        prefix = Pair(prefix.second, char)
    }
    return result
}

fun <T> Map<T, Int>.weightedChoice(): T {
    val list = mutableListOf<T>()
    for ((item, weight) in entries) {
        repeat(weight) {
            list.add(item)
        }
    }
    return list.random()
}

fun generateText(frequency: Frequency, length: Int): String {
    var first = ('a'..'z').random()
    var second = ('a'..'z').random()
    val text = mutableListOf<Char>()
    repeat(length) {
        val frequencyOfPair = frequency.getOrDefault(Pair(first, second), mapOf())
        val next = try {
            frequencyOfPair.weightedChoice()
        } catch (e: NoSuchElementException) {
            println("No following letter found for '$first$second'")
            ' '
        }
        text.add(next)
        first = second
        second = next
    }
    return text.joinToString("")
}

fun loadText(): String {
    val corpus = mutableListOf<Char>()
    for (char in File("src/main/resources/tryphon/corpus.txt").readText().lowercase()) {
        if (char in 'a'..'z' || char == ' ') {
            corpus.add(char)
        }
    }
    return corpus.joinToString("")
}
