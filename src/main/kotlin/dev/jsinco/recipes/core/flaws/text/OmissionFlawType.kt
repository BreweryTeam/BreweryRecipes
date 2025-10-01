package dev.jsinco.recipes.core.flaws.text

import kotlin.random.Random

class OmissionFlawType : TextFlawType {

    override fun apply(text: String, intensity: Double): String {

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
}