package dev.jsinco.recipes.util

import com.dre.brewery.recipe.*
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
                val displayName = when (it) {
                    is CustomItem -> it.name?.let(LegacyComponentSerializer.legacySection()::deserialize)
                    is SimpleItem -> it.material.itemTranslationKey?.let(Component::translatable)
                    is PluginItem -> Component.text(it.itemId)
                    is CustomMatchAnyItem -> {
                        Component.text(if (it.names.isNullOrEmpty()) it.configId else it.names!!.last())
                    }

                    else -> null
                } ?: Component.text("Unknown")
                put(
                    Ingredient(
                        it.toConfigString().replace("/\\d+", ""), displayName
                    ),
                    it.amount
                )
            }
        }
    }
}