package dev.jsinco.recipes.recipe.flaws.type

import dev.jsinco.recipes.recipe.flaws.FlawConfig
import dev.jsinco.recipes.recipe.flaws.FlawTextModificationWriter
import dev.jsinco.recipes.recipe.flaws.FlawTextModifications
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import kotlin.math.roundToInt
import kotlin.random.Random

object CorrectionFlawType : FlawType {

    val NUMBER_REGEX = "[+-]?([0-9]*[.])?[0-9]+".toRegex()
    val FIRST_NUMBER_REGEX = "^$NUMBER_REGEX".toRegex()


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
        config: FlawConfig
    ): Component {
        val valid = FIRST_NUMBER_REGEX.replace(text, "")
        val invalid = FIRST_NUMBER_REGEX.find(text)?.value
        return invalid?.let {
            Component.text(invalid)
                .decoration(TextDecoration.STRIKETHROUGH, true)
                .append(
                    Component.text(valid)
                        .decoration(TextDecoration.STRIKETHROUGH, false)
                )
        } ?: Component.text(text)
    }

    override fun findFlawModifications(
        component: Component,
        session: FlawType.ModificationFindSession
    ): FlawTextModifications {
        val flawTextModifications = FlawTextModifications()
        val config = session.config
        FlawTextModificationWriter.traverse(component, NUMBER_REGEX) { text, startPos ->
            if ((0..<text.length)
                    .map { startPos + it }
                    .any { !session.appliesTo(startPos) }
            ) {
                return@traverse
            }
            val invalidNumber = transformNumber(text, startPos, config).toString()
            if (invalidNumber == text) {
                return@traverse
            }
            for (i in 0..<invalidNumber.length) {
                var content = invalidNumber[i].toString()
                if (i == invalidNumber.length - 1) {
                    content += if (text.length > invalidNumber.length) {
                        " " + invalidNumber.substring(text.length - invalidNumber.length)
                    } else {
                        " $text"
                    }
                }
                flawTextModifications.write(
                    i + startPos,
                    content,
                    0.2
                )
            }
        }
        return flawTextModifications
    }
}