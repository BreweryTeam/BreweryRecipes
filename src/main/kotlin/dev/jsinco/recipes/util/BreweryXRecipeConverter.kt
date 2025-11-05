package dev.jsinco.recipes.util

import com.dre.brewery.recipe.BRecipe
import com.dre.brewery.recipe.CustomItem
import com.dre.brewery.recipe.RecipeItem
import com.dre.brewery.recipe.SimpleItem
import dev.jsinco.recipes.core.BreweryRecipe
import dev.jsinco.recipes.core.process.Ingredient
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

object BreweryXRecipeConverter {
    fun convert(recipe: BRecipe): BreweryRecipe {
        val recipeBuilder = BreweryRecipe.Builder(recipe.id)
        recipeBuilder.cook(
            recipe.cookingTime * 60L * 20L,
            "water",
            parseIngredients(recipe.ingredients),
        )
        if (recipe.distillruns > 0) {
            recipeBuilder.distill(recipe.distillruns.toLong())
        }
        if (recipe.age > 0) {
            recipeBuilder.age((recipe.age * 20 * 60 * 20).toLong(), recipe.wood.name)
        }
        return recipeBuilder.build()
    }

    private fun parseIngredients(ingredients: List<RecipeItem>): Map<Ingredient, Int> {
        return buildMap {
            ingredients.forEach {
                val displayName = when (val genericIngredient = it.toIngredientGeneric()) {
                    is CustomItem -> genericIngredient.name?.let(LegacyComponentSerializer.legacySection()::deserialize)
                    is SimpleItem -> genericIngredient.material.itemTranslationKey?.let(Component::translatable)
                    else -> null
                } ?: Component.text(it.configId)
                put(
                    Ingredient(
                        it.configId, displayName
                    ),
                    it.amount
                )
            }
        }
    }
}