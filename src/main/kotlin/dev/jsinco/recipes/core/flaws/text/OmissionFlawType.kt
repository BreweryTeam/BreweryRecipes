package dev.jsinco.recipes.core.flaws.text

import net.kyori.adventure.text.Component
import kotlin.random.Random

class OmissionFlawType(val intensity: Double, val seeds: List<Int>) : TextFlawType {

    fun apply(text: String, startPos: Int): String {

        if (intensity <= 0.0) return text // No change at 0 intensity, replace all with ' ' at 100 intensity
        if (intensity >= 100.0) return " ".repeat(text.length)
        val probability = intensity.coerceIn(0.0, 100.0) / 100.0

        return buildString {
            var pos = startPos
            for (character in text) {
                if (character == ' ' || seeds.asSequence()
                        .map { Random(it * pos * character.code) }
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

    override fun applyTo(component: Component): Component {
        return component.replaceText {
            it.match(".*").replacement { matchResult, componentBuilder ->
                val everything = matchResult.group()
                return@replacement Component.text(apply(everything, matchResult.start()))
            }
        }
    }

    override fun intensity() = intensity

    override fun seeds() = seeds
}