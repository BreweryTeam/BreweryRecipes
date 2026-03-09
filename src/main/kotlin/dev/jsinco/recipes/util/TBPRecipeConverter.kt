package dev.jsinco.recipes.util

import dev.jsinco.brewery.api.brew.BrewingStep
import dev.jsinco.brewery.api.breweries.BarrelType
import dev.jsinco.brewery.api.breweries.CauldronType
import dev.jsinco.brewery.api.ingredient.Ingredient
import dev.jsinco.brewery.api.ingredient.IngredientMeta
import dev.jsinco.brewery.api.ingredient.IngredientWithMeta
import dev.jsinco.brewery.api.recipe.Recipe
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager
import dev.jsinco.recipes.integration.TbpBrewingIntegration.getApi
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.process.steps.AgeStep
import dev.jsinco.recipes.recipe.process.steps.CookStep
import dev.jsinco.recipes.recipe.process.steps.DistillStep
import dev.jsinco.recipes.recipe.process.steps.MixStep
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
                is BrewingStep.Age -> recipeBuilder.age(it.time().moment(), it.barrelType().name())
                is BrewingStep.Mix -> recipeBuilder.mix(it.time().moment(), "WATER", mapIngredients(it.ingredients()))
            }
        }
        return recipeBuilder.build()
    }

    fun convert(recipe: BreweryRecipe): List<BrewingStep>? {
        val brewManager = getApi().brewManager
        val ingredientManager = BukkitIngredientManager.INSTANCE
        val output = mutableListOf<BrewingStep>()
        for (step in recipe.steps) {
            when (step) {
                is CookStep -> brewManager.cookingStep(
                    step.cookingTicks, step.ingredients.mapKeys { (key, _) ->
                        IngredientWithMeta(
                            ingredientManager.getIngredient(key.key).join().orElse(null) ?: return null,
                            mapOf(IngredientMeta.DISPLAY_NAME to key.displayName)
                        )
                    },
                    CauldronType.valueOf(step.cauldronType.name)
                )

                is MixStep -> brewManager.mixingStep(step.mixingTicks, step.ingredients.mapKeys { (key, _) ->
                    IngredientWithMeta(
                        ingredientManager.getIngredient(key.key).join().orElse(null) ?: return null,
                        mapOf(IngredientMeta.DISPLAY_NAME to key.displayName)
                    )
                }, CauldronType.valueOf(step.cauldronType.name))

                is AgeStep -> brewManager.agingStep(step.agingTicks, BarrelType.valueOf(step.barrelType.name))

                is DistillStep -> brewManager.distillStep(step.count.toInt())
                else -> return null
            }
        }
        return output
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