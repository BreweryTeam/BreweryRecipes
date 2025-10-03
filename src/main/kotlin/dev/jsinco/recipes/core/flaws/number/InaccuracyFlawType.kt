package dev.jsinco.recipes.core.flaws.number

import dev.jsinco.recipes.core.flaws.FlawConfig
import dev.jsinco.recipes.core.flaws.FlawType
import net.kyori.adventure.text.Component
import kotlin.math.roundToInt
import kotlin.random.Random

class InaccuracyFlawType : FlawType {

    companion object {
        val NUMBER_REGEX = "[+-]?([0-9]*[.])?[0-9]+".toRegex()
    }

    override fun applyTo(component: Component, config: FlawConfig): Component {
        return component.replaceText {
            var pos = 0
            it.match(".+").replacement { matchResult, _ ->
                val everything = matchResult.group()
                val prevPos = pos
                pos += everything.length

                Component.text(
                    NUMBER_REGEX.replace(everything) { numberMatch ->
                        val numberString = numberMatch.value
                        val numberStart = matchResult.start() + prevPos

                        // only apply if extent applies
                        if (!config.extent.appliesTo(numberStart)) {
                            return@replace numberString
                        }

                        val newNumber = changeNumber(numberString.toDouble(), numberStart, config)
                        if (numberString.contains(".")) {
                            newNumber.toString()
                        } else {
                            newNumber.roundToInt().toString()
                        }
                    }
                )
            }
        }
    }

    private fun changeNumber(original: Double, numberStart: Int, config: FlawConfig): Double {

        val intensity = config.intensity
        val seed = config.seed

        if (intensity <= 15.0) return original // No inaccuracy at low intensity

        val scale = intensity.coerceIn(15.0, 100.0) / 100.0
        val range = scale * original / 2

        val rng = Random(seed + numberStart)
        val offset = rng.nextDouble(-range, range)

        return original + offset
    }
}