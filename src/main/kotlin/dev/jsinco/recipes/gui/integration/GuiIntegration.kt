package dev.jsinco.recipes.gui.integration

import dev.jsinco.recipes.core.RecipeView
import org.bukkit.inventory.ItemStack

interface GuiIntegration {
    fun createItem(recipeView: RecipeView): ItemStack?
}