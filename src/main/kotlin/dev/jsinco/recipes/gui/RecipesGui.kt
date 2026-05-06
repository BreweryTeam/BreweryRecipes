package dev.jsinco.recipes.gui

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.recipe.RecipeDisplay
import dev.jsinco.recipes.util.GUIUtil
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder

class RecipesGui(
    private val player: Player,
    val mode: RecipeBookMode,
    private val recipeDisplays: List<RecipeDisplay>,
    private val itemResolver: (RecipeDisplay) -> GuiItem?,
    size: Int = 54
) : InventoryHolder {

    private val inventory = Bukkit.createInventory(this, size, calculateGuiName())

    private val recipesSlots = findRecipeSlots()
    private val pageRecipeCapacity = recipesSlots.size
    private val resolved: MutableList<GuiItem> = mutableListOf()
    private var nextInputIdx = 0
    private var page = 0

    fun nextPage() {
        if (!hasNextPage()) return
        page++
        render()
    }

    fun previousPage() {
        page = (page - 1).coerceAtLeast(0)
        render()
    }

    fun findRecipeSlots(): List<Int> {
        val output = (0..<54).toMutableList()
        for (borderEntry in BreweryRecipes.guiConfig.borders) {
            output.removeAll(borderEntry.key.positions.toList())
        }
        for (guiOverride in BreweryRecipes.guiConfig.overrides) {
            for (slot in GUIUtil.getValidSlots(guiOverride.pos)) {
                output.remove(slot)
            }
        }
        return output.toList()
    }

    private fun resolveUntil(needed: Int) {
        while (resolved.size < needed && nextInputIdx < recipeDisplays.size) {
            val item = itemResolver(recipeDisplays[nextInputIdx])
            nextInputIdx++
            if (item != null) resolved.add(item)
        }
    }

    private fun hasNextPage(): Boolean {
        resolveUntil((page + 1) * pageRecipeCapacity + 1)
        return resolved.size > (page + 1) * pageRecipeCapacity
    }

    fun render() {
        inventory.clear()
        resolveUntil((page + 1) * pageRecipeCapacity)

        for (borderEntry in BreweryRecipes.guiConfig.borders) {
            val borderType = borderEntry.key
            val palette = borderEntry.value
            for (i in 0..<borderType.positions.size) {
                val pos = borderType.positions[i]
                val item = palette.content[i % palette.content.size].generateItem()
                renderItem(GuiItem(item, GuiItem.Type.NO_ACTION), pos)
            }
        }

        for (override in BreweryRecipes.guiConfig.overrides) {
            if (override.type == GuiItem.Type.PREVIOUS_PAGE && page == 0) continue
            if (override.type == GuiItem.Type.NEXT_PAGE && !hasNextPage()) continue
            if (override.type == GuiItem.Type.SET_MODE_FRAGMENTS && mode == RecipeBookMode.FRAGMENTS) continue
            if (override.type == GuiItem.Type.SET_MODE_BREWED && mode == RecipeBookMode.BREWED) continue

            for (slot in GUIUtil.getValidSlots(override.pos)) {
                renderItem(GuiItem(override.item.generateItem(), override.type), slot)
            }
        }

        val start = page * pageRecipeCapacity
        val end = minOf((page + 1) * pageRecipeCapacity, resolved.size)
        for (i in start until end) {
            renderItem(resolved[i], recipesSlots[i - start])
        }
    }

    fun renderItem(guiItem: GuiItem, position: Int) {
        inventory.setItem(position, guiItem.item())
    }

    fun open() = open(player)
    fun open(player: Player) = player.openInventory(inventory)

    fun calculateGuiName(): Component {
        val modeId = mode.identifier()
        val wildcard = player.hasPermission("breweryrecipes.override.view")
        val admin = when (mode) {
            RecipeBookMode.FRAGMENTS -> wildcard || player.hasPermission("breweryrecipes.override.view.fragments")
            RecipeBookMode.BREWED -> wildcard || player.hasPermission("breweryrecipes.override.view.notes")
        }
        return if (admin)
            Component.translatable("breweryrecipes.gui.name.admin.$modeId")
        else Component.translatable("breweryrecipes.gui.name.$modeId")
    }

    override fun getInventory() = inventory
}
