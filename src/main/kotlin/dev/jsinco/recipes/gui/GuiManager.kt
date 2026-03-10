package dev.jsinco.recipes.gui

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.RecipeDisplay
import dev.jsinco.recipes.recipe.RecipeDisplays
import org.bukkit.entity.Player

object GuiManager {

    fun openRecipeGui(player: Player) {
        val recipeDisplays = if (player.hasPermission("recipes.override.view")) {
            Recipes.brewingIntegration.allRecipes()
        } else {
            val completedRecipes = Recipes.completedRecipeManager.getCompletedRecipes(player.uniqueId)
                .associateBy { it.identifier }
            val recipeViews = Recipes.recipeViewManager.getViews(player.uniqueId)
                .associateBy { it.recipeIdentifier }
            Recipes.brewingIntegration.allRecipes()
                .map(BreweryRecipe::recipeKey)
                .mapNotNull { recipeKey ->
                    val recipeDisplays = mutableListOf<RecipeDisplay>()
                    completedRecipes[recipeKey]?.let { recipeDisplays.add(it) }
                    recipeViews[recipeKey]?.let { recipeDisplays.add(it) }
                    if (recipeDisplays.isEmpty()) {
                        null
                    } else {
                        RecipeDisplays(recipeKey, *recipeDisplays.toTypedArray())
                    }
                }
        }

        val gui = RecipesGui(
            player,
            recipeDisplays.mapNotNull {
                Recipes.brewingIntegration.createGuiItem(it)
            }
        )
        gui.render()
        gui.open()
    }

}
