package dev.jsinco.recipes.recipe.flaws.drunkentext

import java.util.function.Predicate
import kotlin.random.Random

data class DrunkenTextReplacement(
    val pattern: Regex,
    val minimumIntensity: Double,
    val probability: Double,
    val replacement: String,
    val faultLevel: Double
) {

    fun replacements(text: String, seed: Int, startPos: Int, filter: Predicate<Int>): List<Replacement> {
        val output = mutableListOf<Replacement>()
        for (match in pattern.findAll(text)) {
            if (Random(seed + startPos + match.range.start).nextDouble() > probability) {
                continue
            }
            val content = match.value
            if ((0..<content.length)
                    .map { it + startPos }
                    .any { !filter.test(it) }
            ) {
                continue
            }
            for (i in 0..<(content.length.coerceAtLeast(replacement.length))) {
                val posReplacement = if (content.length > i - 1) {
                    if (replacement.length <= i) {
                        ""
                    } else {
                        replacement[i].toString()
                    }
                } else {
                    replacement.substring(i)
                }
                output.add(Replacement(i + startPos, posReplacement))
            }
        }
        return output
    }

    data class Replacement(val pos: Int, val replacement: String)
}