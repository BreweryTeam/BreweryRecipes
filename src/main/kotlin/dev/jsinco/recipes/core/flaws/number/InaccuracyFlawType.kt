package dev.jsinco.recipes.core.flaws.number

import dev.jsinco.recipes.core.flaws.FlawConfig
import dev.jsinco.recipes.core.flaws.FlawTextModificationWriter
import dev.jsinco.recipes.core.flaws.FlawTextModifications
import dev.jsinco.recipes.core.flaws.FlawType
import net.kyori.adventure.text.Component
import java.util.function.Predicate
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

object InaccuracyFlawType : FlawType {

    val NUMBER_REGEX = "[+-]?([0-9]*[.])?[0-9]+".toRegex()


    fun transformNumber(text: String, pos: Int, config: FlawConfig): Number {
        val newNumber = changeNumber(text.toDouble(), pos, config)
        return if (text.contains(".")) {
            newNumber
        } else {
            newNumber.roundToInt()
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

    override fun postProcess(
        text: String,
        pos: Int,
        seed: Int
    ): Component {
        return Component.text(text)
    }

    override fun findFlawModifications(
        component: Component,
        config: FlawConfig,
        filter: Predicate<Int>
    ): FlawTextModifications {
        val flawTextModifications = FlawTextModifications()
        FlawTextModificationWriter.traverse(component, NUMBER_REGEX) { text, startPos ->
            if (!config.extent.appliesTo(startPos) || (0..<text.length)
                    .map { startPos }
                    .any { !filter.test(it) }
                ) {
                return@traverse
            }
            val newNumber = transformNumber(text, startPos, config)
            val numberString = newNumber.toString()
            if (text == numberString) {
                return@traverse
            }
            for (i in 0..<text.length) {
                var content = if (i < numberString.length) numberString[i].toString() else ""
                if (i == text.length - 1 && numberString.length > text.length) {
                    content += numberString.substring(i)
                }
                flawTextModifications.write(
                    i + startPos,
                    content,
                    abs(text.toDouble() - newNumber.toDouble()) / newNumber.toDouble()
                )
            }
        }
        return flawTextModifications
    }
}