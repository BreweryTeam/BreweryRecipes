package dev.jsinco.recipes.core.flaws.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import kotlin.random.Random

class ObfuscationFlawType(val intensity: Double, val seeds: List<Int>) : TextFlawType {

    fun apply(text: String, startPos: Int): Component {

        if (intensity <= 0.0) return Component.text(text) // No obfuscation at 0 intensity, full obfuscation at 100
        if (intensity >= 100.0) return Component.text(text).decoration(TextDecoration.OBFUSCATED, true)

        val probability = intensity.coerceIn(0.0, 100.0) / 100.0

        val result = Component.text()
        var obfuscatedPart = Component.text().decoration(TextDecoration.OBFUSCATED, true)
        var inObf = false
        var pos = startPos

        for (ch in text) {
            val obfuscate = ch != ' ' && seeds.asSequence()
                .map { Random(it * ch.code * pos) }
                .all { it.nextDouble() < probability }
            if (obfuscate) {
                if (!inObf) {
                    inObf = true
                }
                obfuscatedPart.append(Component.text(ch))
            } else {
                if (inObf) {
                    result.append(obfuscatedPart)
                    obfuscatedPart = Component.text().decoration(TextDecoration.OBFUSCATED, true)
                    inObf = false
                }
                result.append(Component.text(ch))
            }
            pos++
        }
        result.decoration(TextDecoration.OBFUSCATED, false)

        return result.build()
    }

    override fun applyTo(component: Component): Component {
        return component.replaceText {
            it.match(".*").replacement { matchResult, componentBuilder ->
                val everything = matchResult.group()
                return@replacement apply(everything, matchResult.start())
            }
        }
    }

    override fun intensity() = intensity

    override fun seeds() = seeds
}