package dev.jsinco.recipes.gui

import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

interface Gui : InventoryHolder {

    fun render()

    fun renderItem(guiItem: GuiItem, position: Int) {
        inventory.setItem(position, guiItem.item())
    }

    fun onGuiClick(clickedItem: ItemStack, type: GuiItem.Type)

    fun open(): InventoryView?
    fun open(player: Player) = player.openInventory(inventory)

}