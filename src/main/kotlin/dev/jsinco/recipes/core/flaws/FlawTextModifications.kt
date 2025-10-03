package dev.jsinco.recipes.core.flaws

import java.util.function.Predicate

class FlawTextModifications {

    val modifiedPoints = mutableMapOf<Int, TextModification>()
    var totalLength = 1

    fun intensity(): Double {
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
        out.totalLength = totalLength
        return out
    }

    fun withEmptyReplacements(theFilter: Predicate<Int>): FlawTextModifications {
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
        out.totalLength = totalLength
        return out
    }

    fun offsets(previous: Map<Int, Int>): Map<Int, Int> {
        val output = previous.toMutableMap()
        for (i in 0..<totalLength) {
            val offset = (modifiedPoints[i]?.content()?.length ?: 1) - 1
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