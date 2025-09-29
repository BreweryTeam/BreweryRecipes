package dev.jsinco.recipes.listeners

import com.dre.brewery.api.events.brew.BrewModifyEvent
import com.dre.brewery.utility.Logging
import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.Util
import dev.jsinco.recipes.guis.GuiItemType
import dev.jsinco.recipes.guis.PaginatedGui
import dev.jsinco.recipes.guis.RecipeGui
import dev.jsinco.recipes.recipe.BreweryXRecipe
import dev.jsinco.recipes.recipe.RecipeItem
import dev.jsinco.recipes.recipe.RecipeUtil
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.LootGenerateEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.random.Random


class EventListener(private val plugin: Recipes) : Listener {

    private val BOOK_KEY = NamespacedKey(plugin, "recipe-book")
    private val LEGACY_BOOK_KEY = NamespacedKey("brewery", "recipe-book")
    private val RECIPE_KEY = NamespacedKey(plugin, "recipe-key")
    private val LEGACY_RECIPE_KEY = NamespacedKey("brewery", "recipe-key")

    @EventHandler
    fun onGuiClick(event: InventoryClickEvent) {
        if (event.inventory.holder !is RecipeGui) return
        event.isCancelled = true
        val paginatedGUI: PaginatedGui = (event.inventory.holder as RecipeGui).paginatedGui

        val player: Player = event.whoClicked as Player
        val clickedItem: ItemStack = event.currentItem ?: return

        val guiItemType: GuiItemType = GuiItemType.valueOf(clickedItem.itemMeta?.persistentDataContainer?.get(
            NamespacedKey(plugin,"gui-item-type"), PersistentDataType.STRING) ?: return)

        when (guiItemType) {
            GuiItemType.PREVIOUS_PAGE -> {
                val currentPage = paginatedGUI.indexOf(event.inventory)
                if (currentPage == 0) return
                player.openInventory(paginatedGUI.getPage(currentPage - 1))
            }
            GuiItemType.NEXT_PAGE -> {
                val currentPage = paginatedGUI.indexOf(event.inventory)
                if (currentPage == paginatedGUI.size - 1) return
                player.openInventory(paginatedGUI.getPage(currentPage + 1))
            }
            else -> {}
        }
    }

    @EventHandler
    fun onLootGenerate(event: LootGenerateEvent) {
        val bound = Recipes.recipesConfig.recipeSpawning.bound
        val chance = Recipes.recipesConfig.recipeSpawning.chance
        if (bound <= 0 || chance <= 0) return
        else if (Random.nextInt(bound) > chance) return

        var recipe: BreweryXRecipe = RecipeUtil.getRandomRecipe()
        while (Recipes.recipesConfig.recipeSpawning.blacklistedRecipes.contains(recipe.recipeKey)) {
            recipe = RecipeUtil.getRandomRecipe()
        }
        event.loot.add(RecipeItem(recipe).item)
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK  && event.action != Action.RIGHT_CLICK_AIR) return
        val meta = event.item?.itemMeta ?: return
        val player = event.player
        if (meta.persistentDataContainer.has(BOOK_KEY, PersistentDataType.INTEGER) || meta.persistentDataContainer.has(LEGACY_BOOK_KEY, PersistentDataType.INTEGER)) {
            RecipeGui(player).openRecipeGui(player)
            event.isCancelled = true
            return
        }

        var recipeKey: String? = meta.persistentDataContainer.get(RECIPE_KEY, PersistentDataType.STRING)
        if (recipeKey == null) {
            recipeKey = meta.persistentDataContainer.get(LEGACY_RECIPE_KEY, PersistentDataType.STRING)
        }
        val recipeObj: BreweryXRecipe = RecipeUtil.getRecipeFromKey(recipeKey ?: return) ?: return
        event.isCancelled = true

        if (Util.hasRecipePermission(player, recipeKey)) {
            Logging.msg(player, Recipes.recipesConfig.messages.alreadyLearned.replace("%recipe%", recipeObj.name))
            return
        }

        event.item!!.amount--

        Recipes.permissionManager.setPermission(Recipes.recipesConfig.recipePermissionNode.replace("%recipe%", recipeKey), player, true)
        Logging.msg(player, Recipes.recipesConfig.messages.learned.replace("%recipe%", recipeObj.name))
        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
    }

    @EventHandler
    fun onBrewModify(event: BrewModifyEvent) {
        val player = event.player ?: return

        if (event.type != BrewModifyEvent.Type.FILL && event.type != BrewModifyEvent.Type.CREATE) {
            return
        }

        val bRecipe = event.brew.currentRecipe
        val recipeKey: String = bRecipe.id

        if (Recipes.recipesConfig.learnRecipeUponCreation && Recipes.recipesConfig.requireRecipePermissionToBrew) {
            Logging.errorLog("You have two conflicting options enabled: `learnRecipeUponCreation` and `requireRecipePermissionToBrew`. Please disable one of them.")
            return
        }

        if (Recipes.recipesConfig.learnRecipeUponCreation) {
            handleLearnUponRecipeCreation(player, recipeKey)
        } else if (Recipes.recipesConfig.requireRecipePermissionToBrew) {
            handleRequireRecipePermissionToBrew(player, recipeKey, event)
        }
    }

    // Honestly, it's a little messy, but I really do little with
    //  this addon anyway.

    private fun handleLearnUponRecipeCreation(player: Player, recipeKey: String) {
        if (Util.hasRecipePermission(player, recipeKey)) {
            return
        }

        Recipes.permissionManager.setPermission(Recipes.recipesConfig.recipePermissionNode.replace("%recipe%", recipeKey), player, true)
        Logging.msg(player, Recipes.recipesConfig.messages.learned.replace("%recipe%", RecipeUtil.getRecipeFromKey(recipeKey)?.name ?: recipeKey))
        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
    }

    private fun handleRequireRecipePermissionToBrew(player: Player, recipeKey: String, event: BrewModifyEvent) {
        if (!Util.hasRecipePermission(player, recipeKey)) {
            event.isCancelled = true
            Logging.msg(player, Recipes.recipesConfig.messages.notLearned.replace("%recipe%", RecipeUtil.getRecipeFromKey(recipeKey)?.name ?: recipeKey))
        }
    }
}