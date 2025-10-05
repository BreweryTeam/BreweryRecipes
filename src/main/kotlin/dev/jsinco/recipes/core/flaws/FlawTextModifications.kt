package dev.jsinco.recipes.core.flaws

import java.util.function.Predicate

class FlawTextModifications {

    val modifiedPoints = mutableMapOf<Int, TextModification>()

    fun intensity(totalLength: Int): Double {
        return modifiedPoints.values.sumOf { it.intensity() } / totalLength
    }

    fun write(pos: Int, content: String, intensity: Double) {
        modifiedPoints[pos] = TextSubstitution(content, intensity)
    }

    fun contains(pos: Int): Boolean {
        return modifiedPoints.contains(pos)
    }

    fun modifies(pos: Int): Boolean {
        val modification = modifiedPoints[pos] ?: return false
        return modification !is NoModification
    }

    fun get(pos: Int): String? {
        return modifiedPoints[pos]?.content()
    }

    fun withMatching(theFilter: Predicate<Int>): FlawTextModifications {
        val out = FlawTextModifications()
        out.modifiedPoints.putAll(modifiedPoints.filter { theFilter.test(it.key) })
        return out
    }

    fun withNoReplacementsOn(theFilter: Predicate<Int>): FlawTextModifications {
        val out = FlawTextModifications()
        out.modifiedPoints.putAll(
            modifiedPoints.map { entry ->
                entry.key to
                        if (theFilter.test(entry.key)) {
                            NoModification
                        } else {
                            entry.value
                        }
            }
        )
        return out
    }

    fun offsets(previous: Map<Int, Int>): Map<Int, Int> {
        val output = mutableMapOf<Int, Int>()
        if (modifiedPoints.isEmpty()) {
            return previous
        }
        var offset = 0
        val stopPoint = if (previous.isEmpty()) {
            modifiedPoints.keys.max() + 1
        } else {
            modifiedPoints.keys.max()
                .coerceAtLeast(previous.keys.max()) + 1
        }
        for (i in 0..<stopPoint) {
            val newOffset = (modifiedPoints[i]?.content()?.length ?: 1) - 1
            if (newOffset == 0 && !previous.contains(i)) {
                continue
            }
            offset = (previous[i] ?: offset) + newOffset
            if (offset != 0) {
                output[i] = offset
            }
        }
        return output
    }

    interface TextModification {

        fun content(): String?

        fun intensity(): Double
    }

    data class TextSubstitution(val content: String, val intensity: Double) : TextModification {
        override fun content() = content

        override fun intensity() = intensity
    }

    object NoModification : TextModification {
        override fun content() = null

        override fun intensity() = 0.0
    }

}