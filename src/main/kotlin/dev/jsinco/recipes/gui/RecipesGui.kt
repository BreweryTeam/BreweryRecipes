package dev.jsinco.recipes.gui

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class RecipesGui(
    private val player: Player,
    private val recipeItems: List<RecipeItem>,
    title: String = "Recipes",
    size: Int = 54
) : InventoryHolder {

    private val inventory = Bukkit.createInventory(this, size, Component.text(title))
    private val renderedGuiItems = mutableSetOf<GuiItem>()

    private val maxPages = Math.ceilDiv(recipeItems.size, 9 * 4)
    private var page = 0

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
        val recipes = recipeItems
        val pageContentSize = inventory.size - 18
        val pages = Math.ceilDiv(recipes.size, pageContentSize)
        if (page < pages) {
            renderItem(NextPage, 6)
        }
        if (page > 0) {
            renderItem(PreviousPage, 4)
        }
        val startPageContentIndex = Math.floorDiv(recipes.size, pageContentSize) * recipes.size
        val recipesToRead = recipes.size - startPageContentIndex
        for (i in 0 until recipesToRead) {
            renderItem(recipes[i + startPageContentIndex], i + 9)
        }
    }

    fun renderItem(guiItem: GuiItem, position: Int) {
        val item = guiItem.getItem()
        if (!renderedGuiItems.contains(guiItem)) {
            renderedGuiItems.add(guiItem)
        }
        inventory.setItem(position, item)
    }

    fun fromItemStack(itemStack: ItemStack): GuiItem? {
        val guiItem = renderedGuiItems.find { it.getItem() == itemStack }
        return guiItem
    }

    fun open() = open(player)
    fun open(player: Player) = player.openInventory(inventory)

    override fun getInventory() = inventory
}