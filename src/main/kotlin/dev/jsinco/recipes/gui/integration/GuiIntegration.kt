package dev.jsinco.recipes.gui.integration

import dev.jsinco.recipes.core.RecipeView
import dev.jsinco.recipes.gui.GuiItem
import org.bukkit.inventory.ItemStack

interface GuiIntegration {
    fun createItem(recipeView: RecipeView): ItemStack?

    data class RecipeItem(val identifier: String, val itemStack: ItemStack) : GuiItem {
        override fun createItem(): ItemStack? {
            return itemStack
        }

        override fun type() = "recipe"
    }
}