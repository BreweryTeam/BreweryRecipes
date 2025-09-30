package dev.jsinco.recipes.gui

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object NextPage : GuiItem {
    override fun createItem(): ItemStack {
        return ItemStack(Material.ARROW)
    }

    override fun type(): String {
        return "next_page"
    }
}