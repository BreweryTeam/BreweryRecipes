package dev.jsinco.recipes.listeners

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.gui.CooldownManager
import dev.jsinco.recipes.gui.GuiItem
import dev.jsinco.recipes.gui.GuiManager
import dev.jsinco.recipes.gui.RecipesGui
import dev.jsinco.recipes.util.BookUtil
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class GuiEventListener : Listener {

    companion object {
        val GUI_ITEM_TYPE = BreweryRecipes.key("gui_item_type")
    }

    @EventHandler
    fun onGuiClick(event: InventoryClickEvent) {
        val gui = event.inventory.getHolder(false) as? RecipesGui ?: return
        val clickedItem: ItemStack = event.currentItem ?: return
        event.isCancelled = true

        if (clickedItem.persistentDataContainer.has(GUI_ITEM_TYPE, PersistentDataType.STRING)) {
            val value = clickedItem.persistentDataContainer.get(GUI_ITEM_TYPE, PersistentDataType.STRING) ?: return
            val type = GuiItem.Type.entries.firstOrNull { it.identifier() == value } ?: return
            val player = event.whoClicked as? Player ?: return
            when (type) {
                GuiItem.Type.NEXT_PAGE -> if (CooldownManager.tryPageSwitch(player)) gui.nextPage()
                GuiItem.Type.PREVIOUS_PAGE -> if (CooldownManager.tryPageSwitch(player)) gui.previousPage()
                GuiItem.Type.SWITCH_MODE -> {
                    if (CooldownManager.tryModeSwitch(player)) GuiManager.openWithMode(player, gui.mode.next())
                }
                else -> {
                    val targetMode = type.targetMode() ?: return
                    if (CooldownManager.tryModeSwitch(player)) GuiManager.openWithMode(player, targetMode)
                }
            }
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        CooldownManager.clearFor(event.player.uniqueId)
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

        GuiManager.openRecipeGui(player)

        event.setUseInteractedBlock(Event.Result.DENY)
        event.setUseItemInHand(Event.Result.DENY)
        event.isCancelled = true
    }
}