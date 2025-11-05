package dev.jsinco.recipes.listeners

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.core.BreweryRecipe
import dev.jsinco.recipes.gui.GuiItem
import dev.jsinco.recipes.gui.RecipesGui
import dev.jsinco.recipes.gui.integration.GuiIntegration
import dev.jsinco.recipes.util.BookUtil
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.LootGenerateEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.random.Random


class GuiEventListener(private val plugin: Recipes, private val guiIntegration: GuiIntegration) : Listener {

    companion object {
        val GUI_ITEM_TYPE = Recipes.key("gui_item_type")
    }

    @EventHandler
    fun onGuiClick(event: InventoryClickEvent) {
        val gui = event.inventory.getHolder(false) as? RecipesGui ?: return
        val clickedItem: ItemStack = event.currentItem ?: return
        event.isCancelled = true

        if (clickedItem.persistentDataContainer.has(GUI_ITEM_TYPE, PersistentDataType.STRING)) {
            val value = clickedItem.persistentDataContainer.get(GUI_ITEM_TYPE, PersistentDataType.STRING) ?: return
            val type = GuiItem.Type.entries.first { it.identifier() == value } ?: return
            when (type) {
                GuiItem.Type.NEXT_PAGE -> gui.nextPage()
                GuiItem.Type.PREVIOUS_PAGE -> gui.previousPage()
                else -> {} // NO-OP
            }
        }
    }

    @EventHandler
    fun onGuiDrag(event: InventoryDragEvent) {
        if (event.inventory.getHolder(false) !is RecipesGui) return
        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_AIR) return
        val item = event.item ?: return
        val player = event.player
        if (!BookUtil.isBook(item)) {
            return
        }

        val recipeViews = if (player.hasPermission("recipes.override.view")) {
            Recipes.recipes().values.map { breweryRecipe -> breweryRecipe.generateCompletedView() }
        } else {
            Recipes.recipeViewManager.getViews(player.uniqueId)
        }

        val gui = RecipesGui(
            player,
            recipeViews.mapNotNull {
                guiIntegration.createItem(it)
            }
        )
        gui.render()
        gui.open()

        event.setUseInteractedBlock(Event.Result.DENY)
        event.setUseItemInHand(Event.Result.DENY)
        event.isCancelled = true
    }
}