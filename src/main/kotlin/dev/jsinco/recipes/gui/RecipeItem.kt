package dev.jsinco.recipes.gui


import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

// Constructor can be simplified to just include the recipe and then we make the
// recipe item based on a certain template from config.
class RecipeItem(val itemStack: ItemStack) : GuiItem() {
    override fun createItem(): ItemStack {
        return itemStack
    }

    override fun handle(event: InventoryClickEvent, gui: RecipesGui) {
        // do nothing
    }
}