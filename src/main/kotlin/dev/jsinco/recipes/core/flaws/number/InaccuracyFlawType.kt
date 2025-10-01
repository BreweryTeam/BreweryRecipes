package dev.jsinco.recipes.core.flaws.number

import kotlin.math.roundToLong
import kotlin.random.Random

class InaccuracyFlawType(val intensity: Double) : NumberFlawType {

    override fun applyTo(number: Long): Long {

        if (intensity <= 0.0) return number // No inaccuracy at 0 intensity

        // Makes the number deviate from its original value.
        // Max intensity (100) results in a random number
        // between half and double the original value.

        val scale = intensity.coerceIn(0.0, 100.0) / 100.0
        val min = number - (number / 2) * scale
        val max = number + (number / 2) * scale

        return Random.nextDouble(min, max).roundToLong()
    }
}