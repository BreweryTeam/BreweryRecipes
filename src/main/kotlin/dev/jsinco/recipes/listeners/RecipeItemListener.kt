package dev.jsinco.recipes.listeners

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.recipe.RecipeView
import dev.jsinco.recipes.util.BookUtil
import dev.jsinco.recipes.util.RecipeItemUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.CraftingInventory
import org.bukkit.inventory.ItemStack

class RecipeItemListener : Listener {

    companion object {
        private val MERGE_CLICKS = setOf(ClickType.LEFT, ClickType.RIGHT, ClickType.CREATIVE)
    }

    private val config get() = BreweryRecipes.recipesConfig.recipeItems

    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_AIR) return
        if (!config.enabled || !config.redeemOnRightClick) return

        val item = event.item ?: return
        if (!RecipeItemUtil.isRecipeItem(item)) return

        val player = event.player
        val recipeView = RecipeItemUtil.readView(item)?.takeIf { RecipeItemUtil.isKnownRecipe(it) } ?: run {
            player.sendMessage(Component.translatable("breweryrecipes.spawning.item.expired"))
            return
        }
        if (BreweryRecipes.recipeViewManager.getViews(player.uniqueId)
                .filter { it.recipeIdentifier == recipeView.recipeIdentifier }
                .any { it.fragmentation() == 0.0 }
        ) {
            player.sendMessage(Component.translatable("breweryrecipes.spawning.item.already-complete"))
            return
        }

        mergeIntoBook(player, recipeView, "breweryrecipes.spawning.item.redeemed")
        item.amount -= 1
    }

    @EventHandler(ignoreCancelled = true)
    fun onRecipeBookClick(event: InventoryClickEvent) {
        if (!config.enabled || !config.mergeOntoRecipeBook) return
        //if (event.clickedInventory !is PlayerInventory) return
        if (event.click !in MERGE_CLICKS) return

        val clicked = event.currentItem ?: return
        if (!BookUtil.isBook(clicked)) return

        val cursor = event.cursor
        if (!RecipeItemUtil.isRecipeItem(cursor)) return

        val player = event.whoClicked as? Player ?: return
        val recipeView = RecipeItemUtil.readView(cursor)?.takeIf { RecipeItemUtil.isKnownRecipe(it) } ?: run {
            player.sendMessage(Component.translatable("breweryrecipes.spawning.item.expired"))
            return
        }

        event.isCancelled = true
        mergeIntoBook(player, recipeView)

        // Someone please fix events in creative mode
        if (event.click != ClickType.CREATIVE) {
            val remaining = cursor.clone().also { it.amount -= 1 }
            player.setItemOnCursor(if (remaining.isEmpty) null else remaining)
        }
        player.updateInventory()
    }

    @EventHandler
    fun onPrepareCraft(event: PrepareItemCraftEvent) {
        if (!config.enabled) return
        if (event.recipe != null) return // don't override registered recipes
        val plan = craftPlan(event.inventory.matrix) ?: return
        event.inventory.result = plan.result
    }

    @EventHandler(ignoreCancelled = true)
    fun onCraftResultClick(event: InventoryClickEvent) {
        if (event.slotType != InventoryType.SlotType.RESULT) return
        if (!config.enabled) return

        val inventory = event.inventory as? CraftingInventory ?: return
        val player = event.whoClicked as? Player ?: return
        val plan = craftPlan(inventory.matrix) ?: return

        // Don't interfere with any other crafting recipes
        if (inventory.result?.isSimilar(plan.result) != true) return

        event.isCancelled = true
        if (!deliverResult(player, event.click, plan.result)) return
        plan.viewsToMerge.forEach { mergeIntoBook(player, it) }
        consumeMatrix(inventory)
        player.updateInventory()
    }

    private data class RecipeCraftPlan(val result: ItemStack, val viewsToMerge: List<RecipeView>)

    private fun craftPlan(matrix: Array<ItemStack?>): RecipeCraftPlan? {
        val contents = matrix.filterNotNull().filter { !it.isEmpty }
        val books = contents.filter { BookUtil.isBook(it) }
        val recipeItems = contents.filter { RecipeItemUtil.isRecipeItem(it) }
        if (recipeItems.isEmpty() || books.size + recipeItems.size != contents.size) return null
        val recipeViews = recipeItems.mapNotNull { RecipeItemUtil.readView(it) }
            .filter { RecipeItemUtil.isKnownRecipe(it) }
        if (recipeViews.size != recipeItems.size) return null // shouldn't happen

        if (books.size == 1) {
            if (!config.mergeInCraftingGrid) return null
            return RecipeCraftPlan(
                books.first().clone().also { it.amount = 1 },
                recipeViews.groupBy { it.recipeIdentifier }
                    .values
                    .mapNotNull { RecipeItemUtil.mergeManyViews(it) }
            )
        }
        if (books.isEmpty() && recipeViews.size > 1) {
            if (!config.combineInCraftingGrid) return null
            val merged = RecipeItemUtil.mergeManyViews(recipeViews) ?: return null
            return RecipeCraftPlan(RecipeItemUtil.createRecipeItem(merged) ?: return null, listOf())
        }
        return null
    }

    private fun deliverResult(player: Player, click: ClickType, result: ItemStack): Boolean {
        return when (click) {
            ClickType.LEFT, ClickType.RIGHT -> {
                val cursor = player.itemOnCursor
                when {
                    cursor.isEmpty -> {
                        player.setItemOnCursor(result)
                        true
                    }
                    cursor.isSimilar(result) && cursor.amount < cursor.maxStackSize -> {
                        player.setItemOnCursor(cursor.clone().also { it.amount += 1 })
                        true
                    }
                    else -> false
                }
            }
            ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT -> player.inventory.addItem(result).isEmpty()
            else -> false
        }
    }

    private fun consumeMatrix(inventory: CraftingInventory) {
        inventory.result = null // prevent dupes
        val matrix = inventory.matrix
        for (i in matrix.indices) {
            val stack = matrix[i] ?: continue
            if (stack.isEmpty) continue
            val remaining = stack.clone().also { it.amount -= 1 }
            matrix[i] = if (remaining.isEmpty) null else remaining
        }
        inventory.matrix = matrix
    }

    private fun mergeIntoBook(
        player: Player,
        recipeView: RecipeView,
        translationKey: String = "breweryrecipes.recipe-item.merged"
    ) {
        BreweryRecipes.recipeViewManager.insertOrMergeView(player.uniqueId, recipeView)
        player.sendMessage(
            Component.translatable(
                translationKey,
                Argument.component(
                    "recipe_name",
                    BreweryRecipes.brewingIntegration.brewDisplayName(recipeView.recipeIdentifier)
                        ?: Component.text(recipeView.recipeIdentifier)
                )
            )
        )
    }
}
