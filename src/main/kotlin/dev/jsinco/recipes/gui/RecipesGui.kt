package dev.jsinco.recipes.gui

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class RecipesGui(player: Player, allItems: List<RecipeItem>, inventory: Inventory) {

    private var page = 0
    private val maxPages = Math.ceilDiv(allItems.size, 9 * 3)

    fun nextPage() {
        page = maxPages.coerceAtMost(page + 1)
        render()
    }

    fun previousPage() {
        page = 0.coerceAtLeast(page - 1)
        render()
    }

    fun render() {

    }
}