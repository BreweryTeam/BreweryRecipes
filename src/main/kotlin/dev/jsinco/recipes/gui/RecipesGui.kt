package dev.jsinco.recipes.gui

import dev.jsinco.recipes.listeners.GuiEventListener
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.persistence.PersistentDataType

class RecipesGui(private val player: Player, private val allItems: List<RecipeItem>, private val inventory: Inventory) {

    private var page = 0
    private val maxPages = Math.ceilDiv(allItems.size, 9 * 4)

    fun nextPage() {
        page = maxPages.coerceAtMost(page + 1)
        render()
    }

    fun previousPage() {
        page = 0.coerceAtLeast(page - 1)
        render()
    }

    fun render() {
        inventory.clear()
        val recipes = allItems
        val pageContentSize = inventory.size - 18
        val pages = Math.ceilDiv(recipes.size, pageContentSize)
        if (page < pages) {
            render(NextPage, 6)
        }
        if (page > 0) {
            render(PreviousPage, 4)
        }
        val startPageContentIndex = Math.floorDiv(recipes.size, pageContentSize) * recipes.size
        val recipesToRead = recipes.size - startPageContentIndex
        for (i in 0 until recipesToRead) {
            render(recipes[i + startPageContentIndex], i + 9)
        }
    }

    fun render(guiItem: GuiItem, position: Int) {
        val item = guiItem.createItem() ?: return
        item.editPersistentDataContainer { pdc ->
            pdc.set(
                GuiEventListener.GUI_TYPE,
                PersistentDataType.STRING,
                guiItem.type()
            )
        }
        inventory.setItem(position, item)
    }
}