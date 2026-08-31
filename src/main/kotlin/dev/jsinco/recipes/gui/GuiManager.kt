package dev.jsinco.recipes.gui

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.configuration.RecipeSortOrder
import dev.jsinco.recipes.configuration.Visibility
import dev.jsinco.recipes.recipe.UndiscoveredRecipe
import dev.jsinco.recipes.recipe.RecipeDetails
import dev.jsinco.recipes.recipe.RecipeDisplay
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import kotlin.collections.sortedByDescending

object GuiManager {

    fun openRecipeGui(viewer: Player, target: OfflinePlayer = viewer, admin: Boolean = false) {
        if (!CooldownManager.tryOpen(viewer)) return
        openWithMode(BreweryRecipes.guiConfig.defaultMode, viewer, target, admin)
    }

    fun openWithMode(mode: RecipeBookMode, viewer: Player, target: OfflinePlayer = viewer, admin: Boolean = false) {
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
                        .map { it.recipeKey() }
                        .mapNotNull { identifier ->
                            val details = RecipeDetails.fromConfig(BreweryRecipes.detailsConfig, identifier)
                            when (details.visibility) {
                                Visibility.VISIBLE -> recipeViews[identifier] ?: UndiscoveredRecipe(identifier)
                                Visibility.SECRET -> recipeViews[identifier]
                                Visibility.HIDDEN -> null
                            }
                        }
                }
                RecipeBookMode.BREWED -> {
                    val completedRecipes = BreweryRecipes.completedRecipeManager.getCompletedRecipes(target.uniqueId)
                        .associateBy { it.identifier }
                    BreweryRecipes.brewingIntegration.allRecipes()
                        .map { it.recipeKey() }
                        .mapNotNull { identifier ->
                            val details = RecipeDetails.fromConfig(BreweryRecipes.detailsConfig, identifier)
                            when (details.visibility) {
                                Visibility.VISIBLE, Visibility.SECRET -> completedRecipes[identifier]
                                Visibility.HIDDEN -> null
                            }
                        }
                }
            }
        }

        val gui = RecipesGui(
            viewer,
            target,
            mode,
            admin,
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

        return when (mode) {
            RecipeBookMode.FRAGMENTS if BreweryRecipes.recipesConfig.groupFragmentsByCompleteness ->
                baseSorted.sortedBy { it.fragmentationGroup() }

            RecipeBookMode.BREWED if BreweryRecipes.recipesConfig.groupBrewNotesByScore ->
                baseSorted.sortedByDescending { if (it is UndiscoveredRecipe) -1.0 else it.scoreEquivalent() }

            else -> baseSorted
        }
    }

    private fun plainName(recipeId: String): String {
        val component = BreweryRecipes.brewingIntegration.brewDisplayName(recipeId) ?: return recipeId
        val rendered = GlobalTranslator.render(component, BreweryRecipes.recipesConfig.language)
        return PlainTextComponentSerializer.plainText().serialize(rendered)
    }

}
