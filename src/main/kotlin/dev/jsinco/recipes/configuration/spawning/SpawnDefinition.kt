package dev.jsinco.recipes.configuration.spawning

import dev.jsinco.recipes.configuration.spawning.conditions.SpawnCondition
import dev.jsinco.recipes.configuration.spawning.triggers.SpawnTrigger
import dev.jsinco.recipes.core.flaws.creation.RecipeViewCreator

data class SpawnDefinition(
    val enabled: Boolean? = null,
    val attempts: Int? = null,
    val chance: Double? = null,
    val flaw: RecipeViewCreator.Type? = null,
    val flawless: Boolean = false,
    val recipeBlacklist: List<String>? = null,
    val recipeWhitelist: List<String>? = null,
    val triggers: List<SpawnTrigger>? = null,
    val conditions: List<SpawnCondition>? = null,
    val conditionBlacklist: List<SpawnCondition>? = null,
) {
    enum class SpawnConfigType {
        CONTAINER, CHEST, BARREL, MINECART, FISHING, MOB_DROP, BLOCK_DROP, LOOT
    }
}
