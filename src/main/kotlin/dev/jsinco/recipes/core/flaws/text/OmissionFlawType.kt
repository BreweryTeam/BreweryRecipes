package dev.jsinco.recipes.core.flaws.text

import dev.jsinco.recipes.core.flaws.FlawConfig
import dev.jsinco.recipes.core.flaws.FlawType
import net.kyori.adventure.text.Component
import kotlin.random.Random

class OmissionFlawType : FlawType {

    private fun apply(text: String, startPos: Int, config: FlawConfig): String {

        val extent = config.extent
        val intensity = config.intensity
        val seed = config.seed

        if (intensity <= 0.0) return text // No change at 0 intensity, replace all with ' ' at 100 intensity
        if (intensity >= 100.0) return " ".repeat(text.length)

        val probability = intensity.coerceIn(0.0, 100.0) / 100.0

        return buildString {
            var pos = startPos
            for (character in text) {
                val rng = Random(seed + pos + character.code)
                if (character == ' ' || !extent.appliesTo(pos) || rng.nextDouble() >= probability) {
                    append(character)
                } else {
                    append(' ')
                }
                pos++
            }
        }
    }

    override fun applyTo(component: Component, config: FlawConfig): Component {
        return component.replaceText {
            var pos = 0
            it.match(".+").replacement { matchResult, _ ->
                val everything = matchResult.group()
                val prevPos = pos
                pos += everything.length
                Component.text(apply(everything, matchResult.start() + prevPos, config))
            }
        }
    }

}