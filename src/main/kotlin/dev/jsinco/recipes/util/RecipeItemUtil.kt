package dev.jsinco.recipes.util

import com.google.gson.JsonParser
import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.configuration.ConfigItem
import dev.jsinco.recipes.data.serdes.RecipeViewSerdes
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.RecipeView
import dev.jsinco.recipes.recipe.RecipeViewLoreWriter
import dev.jsinco.recipes.recipe.flaws.creation.RecipeViewCreator
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.random.Random

object RecipeItemUtil {

    const val DEFAULT_MINIMUM_FLAW_LEVEL = 0.0
    const val DEFAULT_MAXIMUM_FLAW_LEVEL = 100.0

    fun isRecipeItem(item: ItemStack): Boolean {
        return !item.isEmpty && item.persistentDataContainer.has(PdcKeys.RECIPE_VIEW_KEY, PersistentDataType.STRING)
    }

    fun readView(item: ItemStack): RecipeView? {
        val serialized = item.persistentDataContainer.get(PdcKeys.RECIPE_VIEW_KEY, PersistentDataType.STRING) ?: return null
        return try {
            RecipeViewSerdes.deserializeRecipeView(JsonParser.parseString(serialized))
        } catch (_: Exception) {
            null
        }
    }

    fun isKnownRecipe(recipeView: RecipeView): Boolean {
        return BreweryRecipes.brewingIntegration.getRecipe(recipeView.recipeIdentifier) != null
    }

    fun rollView(
        breweryRecipe: BreweryRecipe,
        flawType: RecipeViewCreator.Type? = null,
        flawLevel: Double? = null,
        random: Random = Random.Default
    ): RecipeView {
        val type = flawType ?: RecipeViewCreator.Type.entries.random(random)
        val level = flawLevel ?: random.nextDouble(DEFAULT_MINIMUM_FLAW_LEVEL, DEFAULT_MAXIMUM_FLAW_LEVEL)
        return RecipeViewLoreWriter.clearRedundantFlaws(breweryRecipe.generate(level, type))
    }

    fun createRecipeItem(recipeView: RecipeView, itemOverride: ConfigItem? = null): ItemStack? {
        if (!isKnownRecipe(recipeView)) return null
        val recipeItemsConfig = BreweryRecipes.recipesConfig.recipeItems
        val base = itemOverride ?: recipeItemsConfig.itemOverride.takeIf { it.enabled }?.item
        val item = BreweryRecipes.brewingIntegration.createDisplayItem(recipeView, base?.generateItem()) ?: return null
        if (item.hasData(DataComponentTypes.CONSUMABLE)) {
            item.unsetData(DataComponentTypes.CONSUMABLE) // You could actually get drunk from a recipe otherwise :D
        }
        item.editPersistentDataContainer { pdc ->
            pdc.set(PdcKeys.RECIPE_KEY, PersistentDataType.STRING, recipeView.recipeIdentifier)
            pdc.set(
                PdcKeys.RECIPE_VIEW_KEY,
                PersistentDataType.STRING,
                RecipeViewSerdes.serializeRecipeView(recipeView).toString()
            )
        }
        return item
    }

    fun mergeManyViews(recipeViews: List<RecipeView>): RecipeView? {
        if (recipeViews.isEmpty()) return null
        if (recipeViews.map { it.recipeIdentifier }.distinct().size != 1) return null
        return RecipeViewLoreWriter.clearRedundantFlaws(
            recipeViews.reduce { merged, incoming -> RecipeViewLoreWriter.mergeFlaws(merged, incoming) }
        )
    }
}
