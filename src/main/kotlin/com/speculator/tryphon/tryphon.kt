package com.speculator.tryphon

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

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
                val frequency = computeFrequency(loadText(), 4)
                val text = generateText(frequency, 200)
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

typealias Frequency = Map<List<Char>, Map<Char, Int>>

fun computeFrequency(corpus: String, depth: Int): Frequency {
    val frequency: MutableMap<List<Char>, MutableMap<Char, Int>> = mutableMapOf()
    var prefix = mutableListOf<Char>()
    for (char in corpus) {
        if (prefix.size < depth) {
            prefix.add(char)
        } else {
            val prefixFrequency = frequency.getOrPut(prefix, ::mutableMapOf)
            prefixFrequency[char] = prefixFrequency.getOrDefault(char, 0) + 1
            prefix = prefix.slice(1 until depth).toMutableList().also { it.add(char) }
        }
    }
    return frequency
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

val allowedChars = (('A'..'Z') + ('a'..'z')).toMutableList().apply { addAll(" '.,;:!?".toList()) }.toSet()
val allowedFirstChars = ('A'..'Z')
val allowedSecondChars = ('a'..'z')

// todo: min word size

fun generateText(frequency: Frequency, length: Int): String {
    var prefix: List<Char>
    do {
        prefix = frequency.keys.random()
    } while (prefix[0] !in allowedFirstChars || prefix[1] !in allowedSecondChars)
    val text = prefix.toMutableList()
    while(true) {
        val prefixFrequency = frequency[prefix] ?: throw RuntimeException("No possible char after $prefix")
        val nextChar = prefixFrequency.weightedChoice()
        text.add(nextChar)
        if (text.size >= length && nextChar == '.') {
            break
        }
        prefix = prefix.slice(1 until prefix.size).toMutableList().apply { add(nextChar) }

    }
    return text.joinToString("")
}

fun loadText(): String {
    val corpus = mutableListOf<Char>()
    for (char in File("src/main/resources/tryphon/corpus.txt").readText().replace('\n', ' ').replace("  ", " ").replace("   ", " ")) {
        if (char in allowedChars) {
            corpus.add(char)
        }
    }
    return corpus.joinToString("")
}
