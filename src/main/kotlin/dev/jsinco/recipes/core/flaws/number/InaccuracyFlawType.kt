package dev.jsinco.recipes.core.flaws.number

import dev.jsinco.recipes.core.flaws.FlawExtent
import dev.jsinco.recipes.core.flaws.FlawType
import net.kyori.adventure.text.Component
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.random.Random

class InaccuracyFlawType(val intensity: Double, val seeds: List<Int>) : FlawType {

    companion object {
        val NUMBER_RE = "[+-]?([0-9]*[.])?[0-9]+".toRegex()
    }

    override fun applyTo(component: Component, extent: FlawExtent): Component {
        return component.replaceText {
            var pos = 0
            it.match(".+").replacement { matchResult, componentBuilder ->
                val everything = matchResult.group()
                val prevPos = pos
                pos += everything.length
                Component.text(NUMBER_RE.replace(everything) { numberMatch ->
                    val numberString = numberMatch.value
                    if (!extent.appliesTo(matchResult.start() + prevPos)) {
                        return@replace numberString
                    }
                    val newNumber = changeNumber(numberString.toDouble(), matchResult.start() + prevPos)
                    return@replace if (numberString.contains(".")) newNumber.toString() else
                        newNumber.roundToInt().toString()
                })
            }
        }
    }

    private fun changeNumber(number: Double, numberStart: Int): Double {
        if (intensity <= 20.0 || seeds.isEmpty()) return number // No inaccuracy at 0 intensity

        // Makes the number deviate from its original value.
        // Max intensity (100) results in a random number
        // between half and double the original value.

        val scale = intensity.coerceIn(20.0, 100.0) / 100.0
        val range = scale * number / 2
        val offsets = seeds.asSequence()
            .map { Random(it + numberStart) }
            .map { it.nextDouble(-range, range) }
            .toList()
        val minOffset = offsets.min()
        val maxOffset = offsets.max()

        return number + if (maxOffset.absoluteValue > minOffset.absoluteValue) maxOffset else minOffset
    }

    override fun intensity() = intensity

    override fun seeds() = seeds
}