package dev.jsinco.recipes.gui

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.configuration.GroupPosition
import dev.jsinco.recipes.configuration.SortOrder
import dev.jsinco.recipes.listeners.GuiEventListener
import dev.jsinco.recipes.recipe.BreweryRecipeGroup
import dev.jsinco.recipes.util.GUIUtil
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.collections.sortedWith
import kotlin.comparisons.compareBy

class GroupsGui(
    private val player: Player,
    val target: OfflinePlayer,
    val mode: RecipeBookMode,
    val admin: Boolean,
    size: Int = 54
) : Gui {

    private val inventory = Bukkit.createInventory(this, size, Component.translatable("breweryrecipes.gui.name.groups"))

    private val groups = initGroups()
    private val recipesSlots = GUIUtil.openSlots()
    private val pageRecipeCapacity = recipesSlots.size
    private var page = 0

    private fun initGroups(): List<String?> {
        val groups = BreweryRecipes.brewingIntegration.allGroups()
            .filter { id -> BreweryRecipes.guiConfig.groups.hiddenGroups.none { hidden -> hidden.equals(id, ignoreCase = true) } }
        val sorted: MutableList<String?> = when (BreweryRecipes.guiConfig.groups.groupSortOrder) {
            SortOrder.AS_PROVIDED -> groups
            SortOrder.ALPHABETICAL_IDENTIFIER ->
                groups.sortedWith(String.CASE_INSENSITIVE_ORDER)
            SortOrder.ALPHABETICAL_NAME ->
                groups.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { plainName(it) })
        }.toMutableList()

        if (BreweryRecipes.guiConfig.groups.allRecipesItem.enabled) {
            when (BreweryRecipes.guiConfig.groups.allRecipesPosition) {
                GroupPosition.START -> sorted.addFirst(null)
                GroupPosition.END -> sorted.addLast(null)
            }
        }
        return sorted
    }

    private fun plainName(groupId: String): String {
        val group = BreweryRecipes.brewingIntegration.getGroup(groupId) ?: return groupId
        val rendered = GlobalTranslator.render(group.displayName, BreweryRecipes.recipesConfig.language)
        return PlainTextComponentSerializer.plainText().serialize(rendered)
    }

    private fun nextPage() {
        if (!hasNextPage()) return
        page++
        render()
    }

    private fun previousPage() {
        page = (page - 1).coerceAtLeast(0)
        render()
    }

    private fun hasNextPage(): Boolean {
        return groups.size > (page + 1) * pageRecipeCapacity
    }

    override fun render() {
        inventory.clear()

        GUIUtil.borders().forEach { renderItem(it.first, it.second) }

        BreweryRecipes.guiConfig.overrides.filter { override ->
            when (override.type) {
                GuiItem.Type.PREVIOUS_PAGE -> page > 0
                GuiItem.Type.NEXT_PAGE -> hasNextPage()
                GuiItem.Type.OPEN_ALL_GROUP, GuiItem.Type.OPEN_GROUP, GuiItem.Type.NO_ACTION -> true
                else -> false
            }
        }.forEach { override ->
            for (slot in GUIUtil.getValidSlots(override.pos)) {
                renderItem(GuiItem(override.item.generateItem(), override.type), slot)
            }
        }

        val start = page * pageRecipeCapacity
        val end = minOf((page + 1) * pageRecipeCapacity, groups.size)
        for (i in start until end) {
            val groupId = groups[i]
            if (groupId == null) {
                renderAllGroup(recipesSlots[i - start])
            } else {
                BreweryRecipes.brewingIntegration.getGroup(groupId)?.let { group ->
                    renderGroup(group, recipesSlots[i - start])
                }
            }
        }
    }

    private fun renderAllGroup(position: Int) {
        val item = BreweryRecipes.guiConfig.groups.allRecipesItem.item.generateItem()
        renderItem(GuiItem(item, GuiItem.Type.OPEN_ALL_GROUP), position)
    }

    private fun renderGroup(group: BreweryRecipeGroup, position: Int) {
        val configItem = BreweryRecipes.guiConfig.groups.groupItems[group.id] ?: BreweryRecipes.guiConfig.groups.defaultItem
        val item = configItem.generateItem()
        item.setData(
            DataComponentTypes.ITEM_NAME,
            GlobalTranslator.render(group.displayName, BreweryRecipes.recipesConfig.language)
        )
        item.editPersistentDataContainer { pdc -> pdc.set(GuiEventListener.GUI_GROUP, PersistentDataType.STRING, group.id) }
        renderItem(GuiItem(item, GuiItem.Type.OPEN_GROUP), position)
    }

    override fun onGuiClick(clickedItem: ItemStack, type: GuiItem.Type) {
        when (type) {
            GuiItem.Type.NEXT_PAGE -> if (CooldownManager.tryPageSwitch(player)) nextPage()
            GuiItem.Type.PREVIOUS_PAGE -> if (CooldownManager.tryPageSwitch(player)) previousPage()
            GuiItem.Type.OPEN_ALL_GROUP -> {
                if (CooldownManager.tryModeSwitch(player)) {
                    GuiManager.openRecipesGui(mode, player, target, null, admin)
                }
            }
            GuiItem.Type.OPEN_GROUP -> {
                if (CooldownManager.tryModeSwitch(player)) {
                    val groupId = clickedItem.persistentDataContainer[GuiEventListener.GUI_GROUP, PersistentDataType.STRING] ?: return
                    val group = BreweryRecipes.brewingIntegration.getGroup(groupId) ?: return
                    GuiManager.openRecipesGui(mode, player, target, group, admin)
                }
            }
            else -> {}
        }
    }

    override fun open() = open(player)
    override fun getInventory() = inventory

}
