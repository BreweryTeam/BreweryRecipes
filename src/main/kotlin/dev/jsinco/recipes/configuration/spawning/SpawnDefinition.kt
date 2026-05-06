package dev.jsinco.recipes.configuration.spawning

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.configuration.ConfigItem
import dev.jsinco.recipes.configuration.spawning.triggers.TriggersDefinition
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.flaws.creation.RecipeViewCreator
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType

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
    val itemOverride: ConfigItem? = null,
) {

    fun generateItems(): List<ItemStack> {
        val attempts = (attempts ?: 1).coerceAtLeast(1)
        val chance = (chance ?: 1.0).coerceIn(0.0, 1.0)
        val applicableRecipes = applicableRecipes()
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

    private fun applicableRecipes(): List<BreweryRecipe> {
        return BreweryRecipes.brewingIntegration.allRecipes()
            .filter { recipeWhitelist.isNullOrEmpty() || recipeWhitelist.contains(it.identifier) }
            .filter { recipeBlacklist.isNullOrEmpty() || !recipeBlacklist.contains(it.identifier) }
    }

    private fun lootItem(breweryRecipe: BreweryRecipe): ItemStack {
        val itemBase = itemOverride?.generateItem() ?: ItemType.PAPER.createItemStack()
        if (flawless) {
            return breweryRecipe.lootItem(itemBase)
        }
        if (flaws.isNullOrEmpty()) {
            return breweryRecipe.lootItem(itemBase, RecipeViewCreator.Type.entries.toTypedArray().random())
        }
        return breweryRecipe.lootItem(itemBase, flaws.random())
    }

    fun registerRecipe(index: Int) {
        val applicableRecipes = applicableRecipes()
        if (applicableRecipes.isEmpty()) return
        triggers?.craftingTrigger?.craftingDefinition?.register(
            lootItem(applicableRecipes.random()),
            "spawning/index_$index"
        )
    }
}
