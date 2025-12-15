package dev.jsinco.recipes.util

import dev.jsinco.brewery.api.brew.BrewingStep
import dev.jsinco.brewery.api.ingredient.Ingredient
import dev.jsinco.brewery.api.recipe.Recipe
import dev.jsinco.recipes.recipe.BreweryRecipe
import org.bukkit.inventory.ItemStack

object TBPRecipeConverter {


    fun convert(recipe: Recipe<ItemStack>): BreweryRecipe {
        val recipeBuilder = BreweryRecipe.Builder(recipe.recipeName)
        val steps = recipe.steps
        steps.forEach {
            when (it) {
                is BrewingStep.Cook -> recipeBuilder.cook(
                    it.time().moment(),
                    it.cauldronType().name,
                    mapIngredients(it.ingredients())
                )

                is BrewingStep.Distill -> recipeBuilder.distill(it.runs().toLong())
                is BrewingStep.Age -> recipeBuilder.age(it.time().moment(), it.barrelType().name)
                is BrewingStep.Mix -> recipeBuilder.mix(it.time().moment(), "WATER", mapIngredients(it.ingredients()))
            }
        }
        return recipeBuilder.build()
    }

    private fun mapIngredients(ingredients: Map<out Ingredient, Int>): Map<dev.jsinco.recipes.recipe.process.Ingredient, Int> {
        return ingredients.asSequence()
            .map { entry ->
                dev.jsinco.recipes.recipe.process.Ingredient(
                    entry.key.key,
                    entry.key.displayName()
                ) to entry.value
            }
            .toMap()
    }
}