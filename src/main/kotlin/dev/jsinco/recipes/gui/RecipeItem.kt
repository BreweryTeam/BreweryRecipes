package dev.jsinco.recipes.gui

import org.bukkit.inventory.ItemStack

interface RecipeItem : GuiItem {

    fun key(): String

    fun loot(): ItemStack

    override fun type(): String {
        return "recipe"
    }
}