package dev.jsinco.recipes.configuration.spawning

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.configuration.ConfigItem
import dev.jsinco.recipes.configuration.spawning.triggers.TriggersDefinition
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.flaws.creation.RecipeViewCreator
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType
import kotlin.random.Random

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

    private fun lootItem(breweryRecipe: BreweryRecipe, random: Random = Random.Default): ItemStack {
        val itemBase = itemOverride?.generateItem() ?: ItemType.PAPER.createItemStack()
        if (flawless) {
            return breweryRecipe.lootItem(itemBase)
        }
        if (flaws.isNullOrEmpty()) {
            return breweryRecipe.lootItem(itemBase, RecipeViewCreator.Type.entries.random(random))
        }
        return breweryRecipe.lootItem(itemBase, flaws.random(random))
    }

    fun registerRecipe(index: Int) {
        val applicableRecipes = applicableRecipes()
        if (applicableRecipes.isEmpty()) return
        val recipe = applicableRecipes.first()
        val random = Random(recipe.recipeKey().hashCode().toLong()) // ensure crafting recipes are deterministic across reloads
        triggers?.craftingTrigger?.craftingDefinition?.register(
            lootItem(recipe, random),
            "spawning/index_$index"
        )
    }
}
