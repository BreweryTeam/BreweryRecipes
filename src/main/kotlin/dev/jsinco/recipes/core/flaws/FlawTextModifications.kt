package dev.jsinco.recipes.core.flaws

import java.util.function.Predicate

class FlawTextModifications {

    val modifiedPoints = mutableMapOf<Int, TextModification>()
    var totalLength = 1

    fun intensity(): Double {
        return modifiedPoints.values.sumOf { it.intensity } / totalLength
    }

    fun write(pos: Int, content: String, intensity: Double) {
        modifiedPoints[pos] = TextModification(content, intensity)
    }

    fun contains(pos: Int): Boolean {
        return modifiedPoints.contains(pos)
    }

    fun get(pos: Int): String? {
        return modifiedPoints[pos]?.content
    }

    fun withMatching(theFilter: Predicate<Int>): FlawTextModifications {
        val out = FlawTextModifications()
        out.modifiedPoints.putAll(modifiedPoints.filter { theFilter.test(it.key) })
        return out
    }

    fun offsets(previous: Map<Int, Int>): Map<Int, Int> {
        val output = previous.toMutableMap()
        for (i in 0..<totalLength) {
            val offset = (modifiedPoints[i]?.content?.length ?: 1) - 1
            if (offset == 0) {
                continue
            }
            val changedOffset = if (output.isEmpty()) {
                offset
            } else {
                offset + (output[output.filter { it.key < i }.maxOf { it.key }] ?: 0)
            }
            if (changedOffset == 0) {
                output.remove(i)
            } else {
                output[i] = changedOffset
            }
        }
        return output
    }

    data class TextModification(val content: String, val intensity: Double)

}