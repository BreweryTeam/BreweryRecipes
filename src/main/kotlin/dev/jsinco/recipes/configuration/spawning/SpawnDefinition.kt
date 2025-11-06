package dev.jsinco.recipes.configuration.spawning

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.configuration.spawning.triggers.TriggersDefinition
import dev.jsinco.recipes.core.BreweryRecipe
import dev.jsinco.recipes.core.flaws.creation.RecipeViewCreator
import org.bukkit.inventory.ItemStack

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
) {

    fun generateItems(): List<ItemStack> {
        val attempts = (attempts ?: 1).coerceAtLeast(1)
        val chance = (chance ?: 1.0).coerceIn(0.0, 1.0)
        val applicableRecipes = Recipes.recipes().asSequence()
            .filter { recipeWhitelist.isNullOrEmpty() || recipeWhitelist.contains(it.key) }
            .filter { recipeBlacklist.isNullOrEmpty() || !recipeBlacklist.contains(it.key) }
            .map { it.value }
            .toList()
        if (applicableRecipes.isEmpty()) return mutableListOf()
        val results = mutableListOf<ItemStack>()
        repeat(attempts) {
            if (Math.random() <= chance) {
                results.add(lootItem(applicableRecipes.random()))
            }
        }
        return results
    }

    fun generateItem(): ItemStack? {
        val items = generateItems()
        return if (!items.isEmpty()) items.random() else null
    }

    private fun lootItem(breweryRecipe: BreweryRecipe): ItemStack {
        if (flawless) {
            return breweryRecipe.lootItem()
        }
        if (flaws.isNullOrEmpty()) {
            return breweryRecipe.lootItem(RecipeViewCreator.Type.values().random())
        }
        return breweryRecipe.lootItem(flaws.random())
    }
}
