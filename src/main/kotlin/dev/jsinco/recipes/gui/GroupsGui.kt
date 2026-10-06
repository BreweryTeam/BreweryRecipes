package dev.jsinco.recipes.gui

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.configuration.GroupPosition
import dev.jsinco.recipes.configuration.SortOrder
import dev.jsinco.recipes.configuration.Visibility
import dev.jsinco.recipes.listeners.GuiEventListener
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.BreweryRecipeGroup
import dev.jsinco.recipes.recipe.RecipeDetails
import dev.jsinco.recipes.util.ColorUtil
import dev.jsinco.recipes.util.GUIUtil
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.translation.Argument
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.Bukkit
import org.bukkit.Color
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

    class Group(
        val group: BreweryRecipeGroup?,
        val stats: Stats?
    )
    class Stats(
        val unlocked: Int,
        val brewed: Int,
        val perfected: Int,
        val total: Int
    )

    private fun initGroups(): List<Group> {
        val statsFactory = statsFactory()

        val groups = BreweryRecipes.brewingIntegration.allGroups()
            .filter { id -> BreweryRecipes.guiConfig.groups.hiddenGroups.none { hidden -> hidden.equals(id, ignoreCase = true) } }
            .mapNotNull { id -> BreweryRecipes.brewingIntegration.getGroup(id) }
        val sorted = when (BreweryRecipes.guiConfig.groups.groupSortOrder) {
            SortOrder.AS_PROVIDED -> groups
            SortOrder.ALPHABETICAL_IDENTIFIER ->
                groups.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.id })
            SortOrder.ALPHABETICAL_NAME ->
                groups.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) {
                    val rendered = GlobalTranslator.render(it.displayName, BreweryRecipes.recipesConfig.language)
                    PlainTextComponentSerializer.plainText().serialize(rendered)
                })
        }.map { group -> Group(group, statsFactory(group.recipes)) }
            .toMutableList()

        val allRecipes = BreweryRecipes.brewingIntegration.allRecipes()
        if (BreweryRecipes.guiConfig.groups.allRecipesItem.enabled) {
            when (BreweryRecipes.guiConfig.groups.allRecipesPosition) {
                GroupPosition.START -> sorted.addFirst(Group(null, statsFactory(allRecipes)))
                GroupPosition.END -> sorted.addLast(Group(null, statsFactory(allRecipes)))
            }
        }
        return sorted
    }

    private fun statsFactory(): (Collection<BreweryRecipe>) -> Stats? {
        val showStats = !admin && BreweryRecipes.guiConfig.groups.showStats
        if (showStats) {
            val recipeViews = BreweryRecipes.recipeViewManager.getViews(target.uniqueId)
                .associateBy { it.recipeIdentifier }
            val completedRecipes = BreweryRecipes.completedRecipeManager.getCompletedRecipes(target.uniqueId)
                .associateBy { it.identifier }
            return { recipes -> Stats(
                recipes.count { it.identifier in recipeViews },
                recipes.count { it.identifier in completedRecipes },
                recipes.count { (completedRecipes[it.identifier]?.scoreEquivalent() ?: 0.0) >= 1.0 },
                recipes.count {
                    RecipeDetails.fromConfig(BreweryRecipes.detailsConfig, it.identifier).visibility != Visibility.HIDDEN
                }
            ) }
        } else {
            return { _ -> null }
        }
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
            renderGroup(groups[i], recipesSlots[i - start])
        }
    }

    private fun renderGroup(group: Group, position: Int) {
        val recipeGroup = group.group
        val item = if (recipeGroup != null) {
            val configItem = BreweryRecipes.guiConfig.groups.groupItems[recipeGroup.id] ?: BreweryRecipes.guiConfig.groups.defaultItem
            val item = configItem.generateItem()
            item.setData(
                DataComponentTypes.CUSTOM_NAME,
                GlobalTranslator.render(recipeGroup.displayName, BreweryRecipes.recipesConfig.language)
                    .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    .colorIfAbsent(NamedTextColor.WHITE)
            )
            item.editPersistentDataContainer { pdc ->
                pdc.set(GuiEventListener.GUI_GROUP, PersistentDataType.STRING, recipeGroup.id)
            }
            item
        } else {
            BreweryRecipes.guiConfig.groups.allRecipesItem.item.generateItem()
        }

        if (group.stats != null) {
            val lore = listOf(
                line("breweryrecipes.gui.groups.unlocked", group.stats.unlocked, group.stats.total),
                line("breweryrecipes.gui.groups.brewed", group.stats.brewed, group.stats.total),
                line("breweryrecipes.gui.groups.perfected", group.stats.perfected, group.stats.total)
            ).map { component ->
                GlobalTranslator.render(component, BreweryRecipes.recipesConfig.language)
                    .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            }
            item.setData(DataComponentTypes.LORE, ItemLore.lore(lore))
        }

        val type = if (recipeGroup != null) GuiItem.Type.OPEN_GROUP else GuiItem.Type.OPEN_ALL_GROUP
        renderItem(GuiItem(item, type), position)
    }

    private fun line(key: String, current: Int, total: Int): Component {
        val color = ColorUtil.lerp(
            Color.fromRGB(NamedTextColor.RED.value()),
            Color.fromRGB(NamedTextColor.GREEN.value()),
            current.toFloat() / total.toFloat()
        )
        return Component.translatable(key,
            Argument.numeric("current", current),
            Argument.numeric("max", total)
        ).color(TextColor.color(color.asRGB()))
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
