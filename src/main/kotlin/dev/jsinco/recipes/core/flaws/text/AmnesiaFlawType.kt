package dev.jsinco.recipes.core.flaws.text

import kotlin.random.Random

class AmnesiaFlawType : TextFlawType {

    // We can apply this one to numbers after they've been converted into a String

    override fun apply(text: String, intensity: Double): String {

        if (intensity <= 0.0) return text // No change at 0 intensity, replace all with '?' at 100 intensity
        if (intensity >= 100.0) return text.map { if (it == ' ') ' ' else '?' }.joinToString("")

        return buildString {
            for (character in text) {
                if (character == ' ' || Random.nextDouble() >= intensity.coerceIn(0.0, 100.0) / 100.0) {
                    append(character)
                } else {
                    append('?')
                }
            }
        }
    }
}