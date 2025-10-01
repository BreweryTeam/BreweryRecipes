package dev.jsinco.recipes.core.flaws.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import kotlin.random.Random

class ObfuscationFlawType(val intensity: Double) : TextFlawType {

    fun apply(text: String): Component {

        if (intensity <= 0.0) return Component.text(text) // No obfuscation at 0 intensity, full obfuscation at 100
        if (intensity >= 100.0) return Component.text(text).decoration(TextDecoration.OBFUSCATED, true)

        val probability = intensity.coerceIn(0.0, 100.0) / 100.0

        val result = Component.text()
        var inObf = false

        for (ch in text) {
            val obfuscate = Random.nextDouble() < probability && ch != ' '
            if (obfuscate) {
                if (!inObf) {
                    result.decoration(TextDecoration.OBFUSCATED, true)
                    inObf = true
                }
            } else {
                if (inObf) {
                    result.decoration(TextDecoration.OBFUSCATED, false)
                    inObf = false
                }
            }
            result.append(Component.text(ch))
        }

        if (inObf) {
            result.decoration(TextDecoration.OBFUSCATED, false)
        }

        return result.build()
    }

    override fun applyTo(component: Component): Component {
        return component.replaceText {
            it.replacement { matchResult, componentBuilder ->
                val everything = matchResult.group()
                return@replacement apply(everything)
            }
        }
    }
}