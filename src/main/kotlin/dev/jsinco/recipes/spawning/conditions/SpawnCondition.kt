package dev.jsinco.recipes.spawning.conditions

import org.bukkit.Location
import org.bukkit.entity.Entity

interface SpawnCondition {
    data class SpawnContext(
        val location: Location,
        val entity: Entity? = null
    )
    fun matches(context: SpawnContext): Boolean
}