package dev.jsinco.recipes.core.flaws.text

import net.kyori.adventure.text.Component
import kotlin.random.Random

class OmissionFlawType(val intensity: Double) : TextFlawType {

    fun apply(text: String): String {

        if (intensity <= 0.0) return text // No change at 0 intensity, replace all with ' ' at 100 intensity
        if (intensity >= 100.0) return " ".repeat(text.length)

        return buildString {
            for (character in text) {
                if (Random.nextDouble() >= intensity.coerceIn(0.0, 100.0) / 100.0) {
                    append(character)
                } else {
                    append(' ')
                }
            }
        }
    }

    override fun applyTo(component: Component): Component {
        return component.replaceText {
            it.replacement { matchResult, componentBuilder ->
                val everything = matchResult.group()
                return@replacement Component.text(apply(everything))
            }
        }
    }
}