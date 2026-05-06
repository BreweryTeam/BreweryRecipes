package dev.jsinco.recipes.util

import com.google.gson.JsonParser
import dev.jsinco.recipes.BreweryRecipes

object ItemColorUtil {

    private val colors: Map<String, String> by lazy { load() }

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

    fun getHex(ingredientKey: String): String? {
        val normalized = ingredientKey.substringAfterLast(':')
        return colors[normalized]
            ?: colors[ingredientKey]
            ?: colors.entries.firstOrNull { it.key.startsWith(normalized) }?.value
    }
}
