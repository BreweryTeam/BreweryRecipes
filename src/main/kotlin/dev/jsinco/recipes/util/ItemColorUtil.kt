package dev.jsinco.recipes.util

import com.google.gson.JsonParser
import dev.jsinco.recipes.BreweryRecipes

object ItemColorUtil {

    // Texture face suffixes ordered by how well they represent the block as a whole
    private val FACE_SUFFIXES = listOf("top", "side", "front", "bottom", "back", "north", "east", "south", "west")
    private const val FRAME_SUFFIX_RANK = 9
    private const val STAGE_SUFFIX_RANK = FRAME_SUFFIX_RANK + 10_000

    private val colors: Map<String, String> by lazy { load() }
    private val fallbacks: Map<String, String> by lazy { computeFallbacks(colors) }

    private fun load(): Map<String, String> {
        val stream = BreweryRecipes::class.java.getResourceAsStream("/item-colors.json") ?: return emptyMap()
        return try {
            JsonParser.parseReader(stream.reader()).asJsonObject
                .entrySet()
                .associate { it.key to "#${it.value.asString}" }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun computeFallbacks(colors: Map<String, String>): Map<String, String> {
        val fallbacks = mutableMapOf<String, String>()
        val ranks = mutableMapOf<String, Int>()
        for ((texture, color) in colors) {
            val suffixStart = texture.lastIndexOf('_')
            if (suffixStart < 1) continue
            val rank = suffixRank(texture.substring(suffixStart + 1)) ?: continue
            val material = texture.substring(0, suffixStart)
            if (colors.containsKey(material)) continue
            val previousRank = ranks[material]
            if (previousRank != null && previousRank <= rank) continue
            ranks[material] = rank
            fallbacks[material] = color
        }
        return fallbacks
    }

    // How well a texture suffix represents the whole block (lower is better)
    private fun suffixRank(suffix: String): Int? {
        val faceIndex = FACE_SUFFIXES.indexOf(suffix)
        if (faceIndex >= 0) return faceIndex
        if (suffix.startsWith("stage"))
            return frameRank(suffix.removePrefix("stage"), STAGE_SUFFIX_RANK)
        return frameRank(suffix, FRAME_SUFFIX_RANK)
    }

    private fun frameRank(frame: String, rankOffset: Int): Int? {
        if (frame.isEmpty() || frame.length > 4 || !frame.all { it.isDigit() }) return null
        return rankOffset + frame.toInt()
    }

    fun getHex(ingredientKey: String): String? {
        val normalized = ingredientKey.substringAfterLast(':')
        return colors[normalized]
            ?: colors[ingredientKey]
            ?: fallbacks[normalized]
    }
}
