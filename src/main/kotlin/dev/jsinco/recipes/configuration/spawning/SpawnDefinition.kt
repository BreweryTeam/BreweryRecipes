package dev.jsinco.recipes.configuration.spawning

import dev.jsinco.recipes.configuration.spawning.ConditionsDefinition
import dev.jsinco.recipes.configuration.spawning.triggers.TriggersDefinition
import dev.jsinco.recipes.core.flaws.creation.RecipeViewCreator

data class SpawnDefinition(
    val enabled: Boolean? = null,
    val attempts: Int? = null,
    val chance: Double? = null,
    val flaws: List<RecipeViewCreator.Type>? = null,
    val flawless: Boolean = false,
    val recipeBlacklist: List<String>? = null,
    val recipeWhitelist: List<String>? = null,
    val triggers: TriggersDefinition? = null,
    val conditions: ConditionsDefinition? = null,
    val conditionBlacklist: ConditionsDefinition? = null,
)
