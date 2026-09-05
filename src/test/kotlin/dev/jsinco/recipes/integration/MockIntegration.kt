package dev.jsinco.recipes.integration

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.RecipeDisplay
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.inventory.ItemStack

class MockIntegration : BrewingIntegration {

    private val recipes = mutableMapOf<String, BreweryRecipe>()

    fun registerRecipe(recipe: BreweryRecipe) {
        recipes[recipe.identifier] = recipe
    }

    override fun createItem(identifier: String): ItemStack? {
        throw NotImplementedError()
    }

    override fun brewDisplayName(identifier: String): Component? {
        throw NotImplementedError()
    }

    override fun brewIngredientColor(ingredientKey: String): Color? {
        throw NotImplementedError()
    }

    override fun cookingMinuteTicks(): Long {
        return 20 * 60
    }

    override fun agingYearTicks(): Long {
        return 20 * 60 * 20
    }

    override fun allRecipes(): Collection<BreweryRecipe> {
        return recipes.values
    }

    override fun getRecipe(id: String): BreweryRecipe? {
        return recipes[id]
    }

    override fun reload() {
        throw NotImplementedError()
    }

    override fun enable(breweryRecipes: BreweryRecipes) {
        throw NotImplementedError()
    }

    override fun score(recipe: BreweryRecipe): Double {
        throw NotImplementedError()
    }

}
