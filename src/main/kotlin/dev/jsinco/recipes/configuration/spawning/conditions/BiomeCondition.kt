package dev.jsinco.recipes.configuration.spawning.conditions

import java.util.*

class BiomeCondition(
    vararg val biomes: String
) : SpawnCondition {

    override fun matches(context: SpawnCondition.SpawnContext): Boolean {
        val biome = context.location.block.biome
        val id = biome.key().asString().lowercase(Locale.ROOT)
        val shortId = id.removePrefix("minecraft:")
        return biomes.any { it.equals(shortId, true) }
    }
}
