package dev.jsinco.recipes.core.flaws.text

import dev.jsinco.recipes.core.flaws.FlawExtent
import dev.jsinco.recipes.core.flaws.FlawType
import net.kyori.adventure.text.Component
import kotlin.random.Random

class OmissionFlawType(val intensity: Double, val seeds: List<Int>) : FlawType {

    fun apply(text: String, startPos: Int, extent: FlawExtent): String {

        if (intensity <= 0.0) return text // No change at 0 intensity, replace all with ' ' at 100 intensity
        if (intensity >= 100.0) return " ".repeat(text.length)
        val probability = intensity.coerceIn(0.0, 100.0) / 100.0

        return buildString {
            var pos = startPos
            for (character in text) {
                if (character == ' ' || !extent.appliesTo(pos) || seeds.asSequence()
                        .map { Random(it + pos + character.code) }
                        .any { it.nextDouble() >= probability }
                ) {
                    append(character)
                } else {
                    append(' ')
                }
                pos++
            }
        }
    }

    override fun applyTo(component: Component, extent: FlawExtent): Component {
        return component.replaceText {
            var pos = 0
            it.match(".+").replacement { matchResult, componentBuilder ->
                val everything = matchResult.group()
                val prevPos = pos
                pos += everything.length
                return@replacement Component.text(apply(everything, matchResult.start() + prevPos, extent))
            }
        }
    }

    override fun intensity() = intensity

    override fun seeds() = seeds
}