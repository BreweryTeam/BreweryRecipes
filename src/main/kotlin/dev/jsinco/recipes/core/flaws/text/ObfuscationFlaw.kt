package dev.jsinco.recipes.core.flaws.text

import kotlin.random.Random

class ObfuscationFlaw : TextFlaw {

    override fun apply(text: String, intensity: Double): String {

        if (intensity <= 0.0) return text // No obfuscation at 0 intensity, full obfuscation at 100
        if (intensity >= 100.0) return "<obf>$text</obf>"

        val probability = intensity.coerceIn(0.0, 100.0) / 100.0

        val result = StringBuilder()
        var inObf = false

        for (ch in text) {
            val obfuscate = Random.nextDouble() < probability && ch != ' '
            if (obfuscate) {
                if (!inObf) {
                    result.append("<obf>")
                    inObf = true
                }
            } else {
                if (inObf) {
                    result.append("</obf>")
                    inObf = false
                }
            }
            result.append(ch)
        }

        if (inObf) {
            result.append("</obf>")
        }

        return result.toString()
    }
}