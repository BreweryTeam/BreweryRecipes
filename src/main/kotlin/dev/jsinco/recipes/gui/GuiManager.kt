package dev.jsinco.recipes.gui

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.configuration.RecipeSortOrder
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.RecipeDisplay
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

object GuiManager {

    fun openRecipeGui(viewer: Player, target: OfflinePlayer = viewer) {
        if (!CooldownManager.tryOpen(viewer)) return
        openWithMode(BreweryRecipes.guiConfig.defaultMode, viewer, target)
    }

    fun openWithMode(mode: RecipeBookMode, viewer: Player, target: OfflinePlayer = viewer) {
        val admin = when (mode) {
            RecipeBookMode.FRAGMENTS -> viewer.hasPermission("breweryrecipes.override.view.fragments")
            RecipeBookMode.BREWED -> viewer.hasPermission("breweryrecipes.override.view.notes")
        }
        val recipeDisplays: Collection<RecipeDisplay> = if (admin) {
            when (mode) {
                RecipeBookMode.FRAGMENTS -> BreweryRecipes.brewingIntegration.allRecipes().map { it.generateCompletedView() }
                RecipeBookMode.BREWED -> BreweryRecipes.brewingIntegration.allRecipes()
            }
        } else {
            when (mode) {
                RecipeBookMode.FRAGMENTS -> {
                    val recipeViews = BreweryRecipes.recipeViewManager.getViews(target.uniqueId)
                        .associateBy { it.recipeIdentifier }
                    BreweryRecipes.brewingIntegration.allRecipes()
                        .map(BreweryRecipe::recipeKey)
                        .mapNotNull { recipeViews[it] }
                }
                RecipeBookMode.BREWED -> {
                    BreweryRecipes.completedRecipeManager.getCompletedRecipes(target.uniqueId).toList()
                }
            }
        }

        val gui = RecipesGui(
            viewer,
            target,
            mode,
            sortDisplays(recipeDisplays, mode),
            { display ->
                BreweryRecipes.recipeGuiItemCache.resolve(target.uniqueId, display.recipeKey(), admin, mode) {
                    BreweryRecipes.brewingIntegration.createGuiItem(display)
                }
            }
        )
        gui.render()
        gui.open()
    }

    private fun sortDisplays(displays: Collection<RecipeDisplay>, mode: RecipeBookMode): List<RecipeDisplay> {
        val baseSorted = when (BreweryRecipes.recipesConfig.recipeSortOrder) {
            RecipeSortOrder.AS_PROVIDED -> displays.toList()
            RecipeSortOrder.ALPHABETICAL_IDENTIFIER ->
                displays.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.recipeKey() })

            RecipeSortOrder.ALPHABETICAL_NAME ->
                displays.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { plainName(it.recipeKey()) })
        }

        return when {
            mode == RecipeBookMode.FRAGMENTS && BreweryRecipes.recipesConfig.groupFragmentsByCompleteness ->
                baseSorted.sortedBy { fragmentationGroup(it) }

            mode == RecipeBookMode.BREWED && BreweryRecipes.recipesConfig.groupBrewNotesByScore ->
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
        val component = BreweryRecipes.brewingIntegration.brewDisplayName(recipeId) ?: return recipeId
        val rendered = GlobalTranslator.render(component, BreweryRecipes.recipesConfig.language)
        return PlainTextComponentSerializer.plainText().serialize(rendered)
    }

}
