package dev.jsinco.recipes.configuration.spawning.conditions

class WorldCondition(
    vararg val worlds: String
) : SpawnCondition {

    override fun matches(context: SpawnCondition.SpawnContext): Boolean {
        val world = context.location.world ?: return false
        return worlds.any { world.name.equals(it, true) }
    }
}
