package dev.jsinco.recipes.configuration.spawning.conditions

import net.kyori.adventure.key.Key
import org.bukkit.Location
import org.bukkit.entity.Entity

interface SpawnCondition {
    data class SpawnContext(
        val location: Location,
        val entity: Entity? = null,
        val trigger: Trigger,
        val lootTableKey: Key
    )

    enum class Trigger {
        MOB_PLAYER_KILL,
        BLOCK_BREAK,
        FISHING,
        CONTAINER_POPULATE
    }

    fun matches(context: SpawnContext): Boolean
}