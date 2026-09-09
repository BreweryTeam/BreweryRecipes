package dev.jsinco.recipes.configuration.spawning

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.configuration.ConfigItem
import dev.jsinco.recipes.configuration.spawning.triggers.TriggersDefinition
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.flaws.creation.RecipeViewCreator
import dev.jsinco.recipes.util.RecipeItemUtil
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType
import kotlin.random.Random

data class SpawnDefinition(
    val enabled: Boolean? = null,
    val attempts: Int? = null,
    val chance: Double? = null,
    val itemType: SpawnItemType = SpawnItemType.SCRIBBLING,
    val flaws: List<RecipeViewCreator.Type>? = null,
    val flawless: Boolean = false,
    val flawLevelMin: Double? = null,
    val flawLevelMax: Double? = null,
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
        if (itemType == SpawnItemType.RECIPE) {
            recipeItem(breweryRecipe, random)?.let { return it }
        }
        val itemBase = itemOverride?.generateItem() ?: ItemType.PAPER.createItemStack()
        if (flawless) {
            return breweryRecipe.lootItem(itemBase)
        }
        return breweryRecipe.lootItem(itemBase, flawType(random))
    }

    private fun recipeItem(breweryRecipe: BreweryRecipe, random: Random): ItemStack? {
        val recipeView = if (flawless) {
            breweryRecipe.generateCompletedView()
        } else {
            RecipeItemUtil.rollView(breweryRecipe, flawType(random), flawLevel(random), random)
        }
        return RecipeItemUtil.createRecipeItem(recipeView, itemOverride)
    }

    private fun flawType(random: Random): RecipeViewCreator.Type {
        return flaws?.takeIf { it.isNotEmpty() }?.random(random)
            ?: RecipeViewCreator.Type.entries.random(random)
    }

    private fun flawLevel(random: Random): Double {
        val minimum = (flawLevelMin ?: RecipeItemUtil.DEFAULT_MINIMUM_FLAW_LEVEL).coerceIn(0.0, 100.0)
        val maximum = (flawLevelMax ?: RecipeItemUtil.DEFAULT_MAXIMUM_FLAW_LEVEL).coerceIn(0.0, 100.0)
        if (minimum >= maximum) return minimum
        return random.nextDouble(minimum, maximum)
    }

    fun registerRecipe(index: Int, old: SpawnDefinition? = null) {
        val applicableRecipes = applicableRecipes()
        if (applicableRecipes.isEmpty()) return
        val recipe = applicableRecipes.first()
        val random = Random(recipe.recipeKey().hashCode().toLong()) // ensure crafting recipes are deterministic across reloads
        triggers?.craftingTrigger?.craftingDefinition?.register(
            "spawning/index_$index",
            lootItem(recipe, random),
            old?.triggers?.craftingTrigger?.craftingDefinition
        )
    }
}
