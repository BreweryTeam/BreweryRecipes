package dev.jsinco.recipes.listeners

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.core.flaws.creation.RecipeViewCreator
import dev.jsinco.recipes.gui.integration.GuiIntegration
import dev.jsinco.recipes.util.RecipeUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import kotlin.random.Random

data class RecipeListener(val guiIntegration: GuiIntegration) : Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_AIR) return
        val item = event.item ?: return
        if (item.isEmpty) return
        if (!item.persistentDataContainer.has(RecipeUtil.RECIPE_KEY, PersistentDataType.STRING)) {
            return
        }
        val recipeIdentifier = item.persistentDataContainer.get(RecipeUtil.RECIPE_KEY, PersistentDataType.STRING)
        val flaw = if (item.persistentDataContainer.has(RecipeUtil.FLAW_KEY)) {
            val actual = item.persistentDataContainer.get(
                RecipeUtil.FLAW_KEY, PersistentDataType.STRING
            )
            RecipeViewCreator.Type.entries.first {
                it.name.equals(actual, true)
            }
        } else null
        val recipe = Recipes.recipes()[recipeIdentifier] ?: run {
            event.player.sendMessage(Component.translatable("recipes.loot.expired.recipe"))
            return
        }
        val recipeView = flaw?.let {
            recipe.generate(
                Random.nextDouble(50.0, 100.0),
                it
            )
        } ?: recipe.generate(0.0)
        Recipes.recipeViewManager.insertOrMergeView(
            event.player.uniqueId,
            recipeView
        )
        event.player.sendMessage(
            Component.translatable(
                "recipes.loot.discovery",
                Argument.component(
                    "recipe_name",
                    guiIntegration.brewDisplayName(recipe.identifier) ?: Component.text("Unknown")
                )
            )
        )
        item.amount -= 1
    }
}