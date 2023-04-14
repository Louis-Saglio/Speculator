package com.speculator.tbm.vcub

import java.text.Normalizer

fun String.simplify(): String {
    return Normalizer.normalize(lowercase(), Normalizer.Form.NFKD).filter { it in "abcdefghijklmnopqrstuvwxyz" }
}

fun String.computeSimilarityScoreWith(b: String): Int {
    var score = 0
    val lookups = b.asIterable().iterator()
    var currentLookup = lookups.next()
    forEach { bChar ->
        if (bChar == currentLookup) {
            score += 1
            if (lookups.hasNext()) {
                currentLookup = lookups.next()
            } else {
                return@forEach
            }
        }
    }
    if (b.first() == first()) {
        score += 1
    }
    return score
}