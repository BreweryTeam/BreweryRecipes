package dev.jsinco.recipes.gui

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.recipe.BreweryRecipeGroup
import dev.jsinco.recipes.recipe.RecipeDisplay
import dev.jsinco.recipes.util.GUIUtil
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class RecipesGui(
    private val player: Player,
    val target: OfflinePlayer,
    val mode: RecipeBookMode,
    val group: BreweryRecipeGroup?,
    val admin: Boolean,
    private val recipeDisplays: List<RecipeDisplay>,
    private val itemResolver: (RecipeDisplay) -> GuiItem?,
    size: Int = 54
) : Gui {

    private val inventory = Bukkit.createInventory(this, size, mode.guiName(admin, group))

    private val recipesSlots = GUIUtil.openSlots()
    private val pageRecipeCapacity = recipesSlots.size
    private val resolved: MutableList<GuiItem> = mutableListOf()
    private var nextInputIdx = 0
    private var page = 0

    private fun nextPage() {
        if (!hasNextPage()) return
        page++
        render()
    }

    private fun previousPage() {
        page = (page - 1).coerceAtLeast(0)
        render()
    }

    private fun resolveUntil(needed: Int) {
        val displaysThisGroup = if (group == null) recipeDisplays else {
            val recipeKeys = group.recipes.map { it.recipeKey() }
            recipeDisplays.filter { it.recipeKey() in recipeKeys }
        }
        while (resolved.size < needed && nextInputIdx < displaysThisGroup.size) {
            val item = itemResolver(displaysThisGroup[nextInputIdx])
            nextInputIdx++
            if (item != null) resolved.add(item)
        }
    }

    private fun hasNextPage(): Boolean {
        resolveUntil((page + 1) * pageRecipeCapacity + 1)
        return resolved.size > (page + 1) * pageRecipeCapacity
    }

    override fun render() {
        inventory.clear()
        resolveUntil((page + 1) * pageRecipeCapacity)

        GUIUtil.borders().forEach { renderItem(it.first, it.second) }

        BreweryRecipes.guiConfig.overrides.filter { override ->
            when (override.type) {
                GuiItem.Type.PREVIOUS_PAGE -> page > 0
                GuiItem.Type.NEXT_PAGE -> hasNextPage()
                GuiItem.Type.SET_MODE_FRAGMENTS -> mode != RecipeBookMode.FRAGMENTS
                GuiItem.Type.SET_MODE_BREWED -> mode != RecipeBookMode.BREWED
                GuiItem.Type.VIEW_GROUPS -> !BreweryRecipes.brewingIntegration.allGroups().isEmpty()
                GuiItem.Type.SWITCH_MODE, GuiItem.Type.NO_ACTION -> true
                else -> false
            }
        }.forEach { override ->
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

    override fun onGuiClick(clickedItem: ItemStack, type: GuiItem.Type) {
        when (type) {
            GuiItem.Type.NEXT_PAGE -> if (CooldownManager.tryPageSwitch(player)) nextPage()
            GuiItem.Type.PREVIOUS_PAGE -> if (CooldownManager.tryPageSwitch(player)) previousPage()
            GuiItem.Type.VIEW_GROUPS -> {
                GuiManager.openGroupsGui(mode, player, target, admin)
            }
            GuiItem.Type.SWITCH_MODE -> {
                if (CooldownManager.tryModeSwitch(player)) GuiManager.openRecipesGui(mode.next(), player, target, group, admin)
            }
            else -> {
                val targetMode = type.targetMode() ?: return
                if (CooldownManager.tryModeSwitch(player)) GuiManager.openRecipesGui(targetMode, player, target, group, admin)
            }
        }
    }

    override fun open() = open(player)
    override fun getInventory() = inventory

}
