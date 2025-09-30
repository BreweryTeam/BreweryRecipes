package dev.jsinco.recipes.gui

import org.bukkit.inventory.ItemStack

interface GuiItem {

    fun createItem(): ItemStack

    fun type(): String
}