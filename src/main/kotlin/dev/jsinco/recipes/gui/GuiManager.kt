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
        val wildcard = player.hasPermission("recipes.override.view")
        val admin = when (mode) {
            RecipeBookMode.FRAGMENTS -> wildcard || player.hasPermission("recipes.override.view.fragments")
            RecipeBookMode.BREWED -> wildcard || player.hasPermission("recipes.override.view.notes")
        }
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
            sortDisplays(recipeDisplays, mode),
            { display ->
                Recipes.recipeGuiItemCache.resolve(player.uniqueId, display.recipeKey(), admin, mode) {
                    Recipes.brewingIntegration.createGuiItem(display)
                }
            }
        )
        gui.render()
        gui.open()
    }

    private fun sortDisplays(displays: Collection<RecipeDisplay>, mode: RecipeBookMode): List<RecipeDisplay> {
        val baseSorted = when (Recipes.recipesConfig.recipeSortOrder) {
            RecipeSortOrder.AS_PROVIDED -> displays.toList()
            RecipeSortOrder.ALPHABETICAL_IDENTIFIER ->
                displays.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.recipeKey() })

            RecipeSortOrder.ALPHABETICAL_NAME ->
                displays.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { plainName(it.recipeKey()) })
        }

        return when {
            mode == RecipeBookMode.FRAGMENTS && Recipes.recipesConfig.groupFragmentsByCompleteness ->
                baseSorted.sortedBy { fragmentationGroup(it) }

            mode == RecipeBookMode.BREWED && Recipes.recipesConfig.groupBrewNotesByScore ->
                baseSorted.sortedByDescending { it.scoreEquivalent() }

            else -> baseSorted
        }
    }

    private fun fragmentationGroup(display: RecipeDisplay): Int {
        val fragmentation = (1.0 - display.scoreEquivalent()) * 100.0
        return when {
            fragmentation <= 0.0 -> 0
            fragmentation < 25.0 -> 1
            fragmentation < 50.0 -> 2
            fragmentation < 75.0 -> 3
            else -> 4
        }
    }

    private fun plainName(recipeId: String): String {
        val component = Recipes.brewingIntegration.brewDisplayName(recipeId) ?: return recipeId
        val rendered = GlobalTranslator.render(component, Recipes.recipesConfig.language)
        return PlainTextComponentSerializer.plainText().serialize(rendered)
    }

}
