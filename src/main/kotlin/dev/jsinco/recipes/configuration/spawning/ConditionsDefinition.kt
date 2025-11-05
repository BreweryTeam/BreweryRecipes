package dev.jsinco.recipes.configuration.spawning

import org.bukkit.Location
import org.bukkit.block.Biome

data class ConditionsDefinition(val biomeCondition: List<Biome>? = null, val worldCondition: List<String>? = null) {


    fun matchesLocation(location: Location): Boolean {
        return (biomeCondition?.contains(location.block.biome) ?: false)
                || (worldCondition?.any { it.equals(location.world.name, true) } ?: false)
    }
}