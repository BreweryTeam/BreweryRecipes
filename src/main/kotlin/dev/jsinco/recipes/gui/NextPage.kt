package dev.jsinco.recipes.gui

import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

object NextPage : GuiItem() {

    override fun createItem(): ItemStack {
        return ItemStack(Material.ARROW)
    }

    override fun handle(event: InventoryClickEvent, gui: RecipesGui) {
        gui.nextPage()
    }
}