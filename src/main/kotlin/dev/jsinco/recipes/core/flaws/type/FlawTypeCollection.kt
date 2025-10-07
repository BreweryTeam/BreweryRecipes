package dev.jsinco.recipes.core.flaws.type

import dev.jsinco.recipes.core.flaws.FlawConfig
import dev.jsinco.recipes.core.flaws.FlawExtent
import kotlin.random.Random

enum class FlawTypeCollection(vararg val flawTypes: FlawType) {

    ENCRYPTED(ObfuscationFlawType, ReplacementFlawType(" "), ReplacementFlawType("  ")),
    UNCERTAIN(ReplacementFlawType("?"), InaccuracyFlawType, CorrectionFlawType),
    DRUNK(SlurringFlawType, InaccuracyFlawType, ReplacementFlawType(".")) {

        override fun compileExtent(flawType: FlawType, stepSize: Int): FlawExtent {
            return if (flawType is ReplacementFlawType) {
                FlawExtent.AfterPoint(Random.nextInt(stepSize), Random.nextInt(20))
            } else {
                super.compileExtent(flawType, stepSize)
            }
        }

        override fun compileConfig(flawType: FlawType, flawExtent: FlawExtent, targetIntensity: Double): FlawConfig {
            return if (flawType is ReplacementFlawType) {
                return FlawConfig(flawExtent, Random.nextInt(), 100.0)
            } else {
                super.compileConfig(flawType, flawExtent, targetIntensity)
            }
        }
    };

    open fun compileExtent(flawType: FlawType, stepSize: Int): FlawExtent {
        return when (Random.nextInt(4)) {
            0 -> FlawExtent.Everywhere()
            1 -> FlawExtent.WholeStep(Random.nextInt(stepSize))
            2 -> {
                val start = Random.nextInt(25)
                val stop = Random.nextInt(start + 10, start + 25)
                FlawExtent.StepRange(Random.nextInt(stepSize), start, stop)
            }

            3 ->
                FlawExtent.AfterPoint(Random.nextInt(stepSize), Random.nextInt(20))


            else -> FlawExtent.Everywhere()
        }
    }

    open fun compileConfig(flawType: FlawType, flawExtent: FlawExtent, targetIntensity: Double): FlawConfig {
        return FlawConfig(flawExtent, Random.nextInt(), targetIntensity)
    }
}