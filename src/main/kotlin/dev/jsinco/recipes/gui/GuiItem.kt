package dev.jsinco.recipes.gui

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.util.ItemStackUtil
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class GuiItem {

    companion object {
        private val KEY = Recipes.key("gui_item_type")
    }

    fun getItem(): ItemStack {
        return ItemStackUtil.setPersistentData(createItem(), KEY, PersistentDataType.STRING, type())
    }

    fun type(): String {
        // This could be set manually by child class instead
        return this::class.java.simpleName.lowercase()
    }

    protected abstract fun createItem(): ItemStack

    abstract fun handle(event: InventoryClickEvent, gui: RecipesGui)
}