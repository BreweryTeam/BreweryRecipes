package dev.jsinco.recipes.gui

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.recipe.BreweryRecipeGroup
import dev.jsinco.recipes.recipe.RecipeDisplay
import dev.jsinco.recipes.util.ComponentUtil
import dev.jsinco.recipes.util.GUIUtil
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.translation.Argument
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
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
                GuiItem.Type.VIEW_GROUPS, GuiItem.Type.CYCLE_CATEGORY -> BreweryRecipes.brewingIntegration.hasCustomGroups()
                GuiItem.Type.SWITCH_MODE, GuiItem.Type.NO_ACTION -> true
                else -> false
            }
        }.forEach { override ->
            val item = override.item.generateItem()
            if (override.type == GuiItem.Type.CYCLE_CATEGORY) appendCategoryLore(item)
            for (slot in GUIUtil.getValidSlots(override.pos)) {
                renderItem(GuiItem(item, override.type), slot)
            }
        }

        val start = page * pageRecipeCapacity
        val end = minOf((page + 1) * pageRecipeCapacity, resolved.size)
        for (i in start until end) {
            renderItem(resolved[i], recipesSlots[i - start])
        }
    }

    private fun cycleCategory(step: Int): BreweryRecipeGroup? {
        val options = GuiManager.groupOrder(includeAll = true)
        val current = options.indexOfFirst { it?.id == group?.id }.coerceAtLeast(0)
        return options[Math.floorMod(current + step, options.size)]
    }

    private fun appendCategoryLore(item: ItemStack) {
        val categoryName = group?.displayName ?: Component.translatable("breweryrecipes.gui.category.all")
        val extraLore = listOf(
            Component.translatable("breweryrecipes.gui.category.current", Argument.component("category", categoryName)),
            Component.translatable("breweryrecipes.gui.category.usage")
        ).flatMap {
            ComponentUtil.splitLines(GlobalTranslator.render(it, BreweryRecipes.recipesConfig.language))
        }.map {
            it.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .colorIfAbsent(NamedTextColor.WHITE)
        }
        val existing = item.getData(DataComponentTypes.LORE)?.lines() ?: emptyList()
        item.setData(DataComponentTypes.LORE, ItemLore.lore(existing + extraLore))
        item.unsetData(DataComponentTypes.TOOLTIP_DISPLAY)
    }

    override fun onGuiClick(clickedItem: ItemStack, type: GuiItem.Type, click: ClickType) {
        when (type) {
            GuiItem.Type.CYCLE_CATEGORY -> {
                if (!CooldownManager.tryModeSwitch(player)) return
                val next = when {
                    click.isShiftClick -> null
                    click.isRightClick -> cycleCategory(-1)
                    else -> cycleCategory(1)
                }
                GuiManager.openRecipesGui(mode, player, target, next, admin)
            }
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
