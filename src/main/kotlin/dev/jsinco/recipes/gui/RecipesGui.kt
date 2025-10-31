package dev.jsinco.recipes.gui

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.util.GUIUtil
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder

class RecipesGui(
    private val player: Player,
    private val recipes: List<GuiItem>,
    size: Int = 54
) : InventoryHolder {

    private val inventory = Bukkit.createInventory(this, size, Component.translatable("recipes.display.gui.name"))
    private val renderedGuiItems = mutableSetOf<GuiItem>()

    private val recipesSlots = findRecipeSlots()
    private val pageRecipeCapacity = recipesSlots.size
    private val maxPages = Math.ceilDiv(recipes.size, pageRecipeCapacity)
    private var page = 0

    fun nextPage() {
        page = (page + 1).coerceAtMost(maxPages - 1)
        render()
    }

    fun previousPage() {
        page = (page - 1).coerceAtLeast(0)
        render()
    }

    fun findRecipeSlots(): List<Int> {
        val output = (0..<54).toMutableList()
        for (borderEntry in Recipes.guiConfig.borders) {
            output.removeAll(borderEntry.key.positions.toList())
        }
        for (guiOverride in Recipes.guiConfig.overrides) {
            for (slot in GUIUtil.getValidSlots(guiOverride.pos)) {
                output.remove(slot)
            }
        }
        return output.toList()
    }

    fun render() {
        inventory.clear()
        for (borderEntry in Recipes.guiConfig.borders) {
            val borderType = borderEntry.key
            val palette = borderEntry.value
            for (i in 0..<borderType.positions.size) {
                val pos = borderType.positions[i]
                val item = palette.content[i % palette.content.size].generateItem()
                renderItem(GuiItem(item, GuiItem.Type.NO_ACTION), pos)
            }
        }

        for (override in Recipes.guiConfig.overrides) {
            if (override.type == GuiItem.Type.PREVIOUS_PAGE && page == 0) continue
            if (override.type == GuiItem.Type.NEXT_PAGE && page + 1 >= maxPages) continue

            for (slot in GUIUtil.getValidSlots(override.pos)) {
                renderItem(GuiItem(override.item.generateItem(), override.type), slot)
            }
        }

        val startPageContentIndex = page * pageRecipeCapacity
        val recipesToRead = minOf(pageRecipeCapacity, recipes.size - startPageContentIndex)

        for (i in 0 until recipesToRead) {
            renderItem(recipes[i + startPageContentIndex], recipesSlots[i])
        }
    }

    fun renderItem(guiItem: GuiItem, position: Int) {
        val item = guiItem.item()
        if (!renderedGuiItems.contains(guiItem)) {
            renderedGuiItems.add(guiItem)
        }
        inventory.setItem(position, item)
    }

    fun open() = open(player)
    fun open(player: Player) = player.openInventory(inventory)

    override fun getInventory() = inventory
}
