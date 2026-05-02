package dev.jsinco.recipes.gui

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.configuration.RecipeSortOrder
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.RecipeDisplay
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.entity.Player

object GuiManager {

    fun openRecipeGui(player: Player) {
        if (!CooldownManager.tryOpen(player)) return
        openWithMode(player, Recipes.guiConfig.defaultMode)
    }

    fun openWithMode(player: Player, mode: RecipeBookMode) {
        val admin = player.hasPermission("recipes.override.view")
        val recipeDisplays: Collection<RecipeDisplay> = if (admin) {
            when (mode) {
                RecipeBookMode.FRAGMENTS -> Recipes.brewingIntegration.allRecipes().map { it.generateCompletedView() }
                RecipeBookMode.BREWED -> Recipes.brewingIntegration.allRecipes()
            }
        } else {
            when (mode) {
                RecipeBookMode.FRAGMENTS -> {
                    val recipeViews = Recipes.recipeViewManager.getViews(player.uniqueId)
                        .associateBy { it.recipeIdentifier }
                    Recipes.brewingIntegration.allRecipes()
                        .map(BreweryRecipe::recipeKey)
                        .mapNotNull { recipeViews[it] }
                }
                RecipeBookMode.BREWED -> {
                    Recipes.completedRecipeManager.getCompletedRecipes(player.uniqueId).toList()
                }
            }
        }

        val gui = RecipesGui(
            player,
            mode,
            sortDisplays(recipeDisplays),
            { display ->
                Recipes.recipeGuiItemCache.resolve(player.uniqueId, display.recipeKey(), admin, mode) {
                    Recipes.brewingIntegration.createGuiItem(display)
                }
            }
        )
        gui.render()
        gui.open()
    }

    private fun sortDisplays(displays: Collection<RecipeDisplay>): List<RecipeDisplay> {
        return when (Recipes.recipesConfig.recipeSortOrder) {
            RecipeSortOrder.AS_PROVIDED -> displays.toList()
            RecipeSortOrder.ALPHABETICAL_IDENTIFIER ->
                displays.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.recipeKey() })

            RecipeSortOrder.ALPHABETICAL_NAME ->
                displays.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { plainName(it.recipeKey()) })
        }
    }

    private fun plainName(recipeId: String): String {
        val component = Recipes.brewingIntegration.brewDisplayName(recipeId) ?: return recipeId
        val rendered = GlobalTranslator.render(component, Recipes.recipesConfig.language)
        return PlainTextComponentSerializer.plainText().serialize(rendered)
    }

}
