package dev.jsinco.recipes.integration

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.gui.GuiItem
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.RecipeView
import dev.jsinco.recipes.recipe.RecipeViewLoreWriter
import dev.jsinco.recipes.recipe.process.Recipe
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

interface BrewingIntegration {
    fun createGuiItem(recipeView: RecipeView, recipe: BreweryRecipe): GuiItem? {
        return RecipeViewLoreWriter.writeLore(recipeView, this)
            ?.let { GuiItem(it, GuiItem.Type.NO_ACTION) }
    }

    fun createItem(recipeView: RecipeView): ItemStack?
    fun brewDisplayName(identifier: String): Component?
    fun recipeResult(recipe: BreweryRecipe): RecipeResult
    fun cookingMinuteTicks(): Long
    fun agingYearTicks(): Long
    fun getRecipe(id: String): BreweryRecipe?
    fun reload()
    data class RecipeResult(val displayName: Component, val failure: Boolean)

    fun enable(recipes: Recipes)
}