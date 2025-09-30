package dev.jsinco.recipes.listeners

import com.dre.brewery.utility.Logging
import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.gui.RecipeItem
import dev.jsinco.recipes.gui.RecipesGui
import dev.jsinco.recipes.util.BookUtil
import dev.jsinco.recipes.util.RecipesUtil
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.LootGenerateEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.bukkit.persistence.PersistentDataType
import kotlin.random.Random


class GuiEventListener(private val plugin: Recipes) : Listener {

    companion object {
        val trackedInventories = mutableMapOf<Inventory, RecipesGui>()

        val GUI_TYPE = Recipes.key("gui_type")!!
    }

    private val RECIPE_KEY: NamespacedKey = Recipes.key("recipe-key")!!

    @EventHandler
    fun onGuiClick(event: InventoryClickEvent) {
        val gui = trackedInventories[event.inventory] ?: return
        event.isCancelled = true

        val clickedItem: ItemStack = event.currentItem ?: return
        val guiType =
            clickedItem.persistentDataContainer.get(GUI_TYPE, PersistentDataType.STRING)

        when (guiType) {
            "next_page" -> gui.nextPage()

            "previous_page" -> gui.previousPage()

            else -> {}
        }
    }

    @EventHandler
    fun onGuiDrag(event: InventoryDragEvent) {
        trackedInventories[event.inventory] ?: return
        event.isCancelled = true
    }

    @EventHandler
    fun onLootGenerate(event: LootGenerateEvent) {
        val bound = Recipes.recipesConfig.recipeSpawning.bound
        val chance = Recipes.recipesConfig.recipeSpawning.chance
        if (bound <= 0 || chance <= 0) return
        else if (Random.nextInt(bound) > chance) return
        val applicableRecipes = (Recipes.recipesProvider.get()
            .filter { !Recipes.recipesConfig.recipeSpawning.blacklistedRecipes.contains(it.key()) })
        val recipe: RecipeItem? = if (applicableRecipes.isEmpty()) null else applicableRecipes.random()
        recipe?.let {
            event.loot.add(recipe.loot())
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_AIR) return
        val item = event.item ?: return
        val player = event.player
        if (BookUtil.isBook(item)) {
            val view = MenuType.GENERIC_9X6.builder()
                .build(player)
            val topInventory = view.topInventory
            val gui = RecipesGui(player, Recipes.recipesProvider.get(), topInventory)
            gui.render()
            trackedInventories.put(topInventory, gui)
            view.open()
            event.setUseInteractedBlock(Event.Result.DENY)
            event.setUseItemInHand(Event.Result.DENY)
            return
        }

        val recipeKey: String = item.persistentDataContainer.get(RECIPE_KEY, PersistentDataType.STRING)
            ?: return
        event.isCancelled = true

        if (RecipesUtil.hasRecipe(player, recipeKey)) {
            Logging.msg(player, Recipes.recipesConfig.messages.alreadyLearned.replace("%recipe%", recipeKey))
            return
        }

        event.item!!.amount--
        RecipesUtil.registerRecipe(player, recipeKey)
        Logging.msg(player, Recipes.recipesConfig.messages.learned.replace("%recipe%", recipeKey))
        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
    }
}