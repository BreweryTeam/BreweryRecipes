package dev.jsinco.recipes.core.flaws.number

import kotlin.math.roundToLong
import kotlin.random.Random

class InaccuracyFlaw : NumberFlaw {

    override fun apply(number: Long, intensity: Double): Long {

        if (intensity <= 0.0) return number // No inaccuracy at 0 intensity

        // Makes the number deviate from its original value.
        // Max intensity (100) results in a random number
        // between half and double the original value.

        val scale = intensity.coerceIn(0.0, 100.0) / 100.0
        val min = number - (number - number / 2.0) * scale
        val max = number + (number * 2.0 - number) * scale

        return Random.nextDouble(min, max).roundToLong()
    }
}