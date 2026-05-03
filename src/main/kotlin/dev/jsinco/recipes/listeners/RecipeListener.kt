package dev.jsinco.recipes.listeners

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.recipe.flaws.creation.RecipeViewCreator
import dev.jsinco.recipes.util.PdcKeys
import dev.jsinco.recipes.util.RecipeUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import kotlin.random.Random

class RecipeListener : Listener {

    private val legacyRecipeKeys = listOf(
        NamespacedKey("brewery", "recipe-key"),
        NamespacedKey("breweryx", "recipe-key")
    )

    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_AIR) return
        val item = event.item ?: return
        if (item.isEmpty) return

        val pdc = item.persistentDataContainer
        val legacyIdentifier = legacyRecipeKeys.firstNotNullOfOrNull { pdc.get(it, PersistentDataType.STRING) }
        if (Recipes.recipesConfig.migrate && legacyIdentifier != null) {
            val recipe = Recipes.brewingIntegration.getRecipe(legacyIdentifier) ?: run {
                event.player.sendMessage(Component.translatable("recipes.spawning.item.expired"))
                return
            }
            Recipes.recipeViewManager.insertOrMergeView(event.player.uniqueId, recipe.generateCompletedView())
            event.player.sendMessage(
                Component.translatable(
                    "recipes.spawning.item.redeemed",
                    Argument.component(
                        "recipe_name",
                        Recipes.brewingIntegration.brewDisplayName(recipe.identifier) ?: Component.text("Unknown")
                    )
                )
            )
            item.amount -= 1
            return
        }

        if (!pdc.has(PdcKeys.RECIPE_KEY, PersistentDataType.STRING)) return
        val recipeIdentifier = pdc.get(PdcKeys.RECIPE_KEY, PersistentDataType.STRING) ?: return
        val flaw = if (pdc.has(PdcKeys.FLAW_KEY)) {
            val actual = pdc.get(PdcKeys.FLAW_KEY, PersistentDataType.STRING)
            RecipeViewCreator.Type.entries.first { it.name.equals(actual, true) }
        } else null
        val recipe = Recipes.brewingIntegration.getRecipe(recipeIdentifier) ?: run {
            event.player.sendMessage(Component.translatable("recipes.spawning.item.expired"))
            return
        }
        val recipeView = flaw?.let {
            recipe.generate(Random.nextDouble(50.0, 100.0), it)
        } ?: recipe.generate(0.0)
        Recipes.recipeViewManager.insertOrMergeView(event.player.uniqueId, recipeView)
        event.player.sendMessage(
            Component.translatable(
                "recipes.spawning.item.redeemed",
                Argument.component(
                    "recipe_name",
                    Recipes.brewingIntegration.brewDisplayName(recipe.identifier) ?: Component.text("Unknown")
                )
            )
        )
        item.amount -= 1
    }
}