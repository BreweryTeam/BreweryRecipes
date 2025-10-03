package dev.jsinco.recipes.core.flaws.text

import dev.jsinco.recipes.core.flaws.FlawConfig
import dev.jsinco.recipes.core.flaws.FlawType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import kotlin.random.Random

class ObfuscationFlawType : FlawType {

    private fun apply(text: String, startPos: Int, config: FlawConfig): Component {
        val extent = config.extent
        val intensity = config.intensity
        val seed = config.seed

        if (intensity <= 0.0) return Component.text(text)
        if (intensity >= 100.0) return Component.text(text).decoration(TextDecoration.OBFUSCATED, true)

        val probability = (intensity.coerceIn(0.0, 100.0)) / 100.0

        val result = Component.text() // TextComponent.Builder
        val plain = StringBuilder()
        val obf = StringBuilder()
        var inObf = false
        var pos = startPos

        fun flushPlain() {
            if (plain.isNotEmpty()) {
                result.append(Component.text(plain.toString()))
                plain.setLength(0)
            }
        }
        fun flushObf() {
            if (obf.isNotEmpty()) {
                result.append(
                    Component.text(obf.toString())
                        .decoration(TextDecoration.OBFUSCATED, true)
                )
                obf.setLength(0)
            }
        }

        for (ch in text) {
            val obfuscate = ch != ' ' &&
                    extent.appliesTo(pos) &&
                    Random(seed + ch.code + pos).nextDouble() < probability

            if (obfuscate) {
                if (!inObf) {
                    flushPlain()
                    inObf = true
                }
                obf.append(ch)
            } else {
                if (inObf) {
                    flushObf()
                    inObf = false
                }
                plain.append(ch)
            }
            pos++
        }
        if (inObf) flushObf() else flushPlain()

        return result.build()
    }

    override fun applyTo(component: Component, config: FlawConfig): Component {
        return component.replaceText {
            var pos = 0
            it.match(".+").replacement { matchResult, _ ->
                val everything = matchResult.group()
                val prevPos = pos
                pos += everything.length
                apply(everything, matchResult.start() + prevPos, config)
            }
        }
    }
}
