package dev.jsinco.recipes.gui.integration

import dev.jsinco.recipes.recipe.RecipeView
import dev.jsinco.recipes.recipe.RecipeWriter
import dev.jsinco.recipes.gui.GuiItem
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

interface GuiIntegration {
    fun createFullItem(recipeView: RecipeView): GuiItem? {
        return RecipeWriter.writeItem(recipeView, this)
            ?.let { GuiItem(it, GuiItem.Type.NO_ACTION) }
    }

    fun createItem(recipeView: RecipeView): ItemStack?
    fun brewDisplayName(identifier: String): Component?
    fun cookingMinuteTicks(): Long
    fun agingYearTicks(): Long
}