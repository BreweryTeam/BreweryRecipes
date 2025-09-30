package dev.jsinco.recipes.gui

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object PreviousPage : GuiItem {
    override fun createItem(): ItemStack {
        return ItemStack(Material.ARROW)
    }

    override fun type(): String {
        return "previous_page"
    }
}