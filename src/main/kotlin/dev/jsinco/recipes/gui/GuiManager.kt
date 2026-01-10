package dev.jsinco.recipes.gui

import dev.jsinco.recipes.Recipes
import org.bukkit.entity.Player

object GuiManager {

    fun openRecipeGui(player: Player) {
        val recipeViews = if (player.hasPermission("recipes.override.view")) {
            Recipes.recipes().values.map { breweryRecipe -> breweryRecipe.generateCompletedView() }
        } else {
            Recipes.recipeViewManager.getViews(player.uniqueId)
        }

        val gui = RecipesGui(
            player,
            recipeViews.mapNotNull {
                Recipes.guiIntegration.createFullItem(it)
            }
        )
        gui.render()
        gui.open()
    }

}
