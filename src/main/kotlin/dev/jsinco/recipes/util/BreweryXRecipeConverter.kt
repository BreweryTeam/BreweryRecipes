package dev.jsinco.recipes.util

import com.dre.brewery.BIngredients
import com.dre.brewery.BarrelWoodType
import com.dre.brewery.Brew
import com.dre.brewery.recipe.*
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.process.Ingredient
import dev.jsinco.recipes.recipe.process.steps.AgeStep
import dev.jsinco.recipes.recipe.process.steps.CookStep
import dev.jsinco.recipes.recipe.process.steps.DistillStep
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

object BreweryXRecipeConverter {
    fun convert(recipe: BRecipe): BreweryRecipe {
        val recipeBuilder = BreweryRecipe.Builder(recipe.id)
        recipeBuilder.difficulty(recipe.getDifficulty().toDouble())
        recipeBuilder.cook(
            recipe.cookingTime * 60L * 20L,
            "water",
            convertIngredients(recipe.ingredients),
        )
        if (recipe.distillruns > 0) {
            recipeBuilder.distill(recipe.distillruns.toLong())
        }
        if (recipe.age > 0) {
            recipeBuilder.age((recipe.age * 20 * 60 * 20).toLong(), recipe.wood.name)
        }
        return recipeBuilder.build()
    }

    fun convert(recipe: BreweryRecipe): Brew? {
        if (recipe.steps.isEmpty()) return null
        val cookStep = recipe.steps[0] as? CookStep ?: return null
        val bIngredients =
            BIngredients(
                BRecipe.loadIngredients(
                    cookStep.ingredients.map {
                        "${it.key.key.replace("minecraft:", "")}/${it.value}"
                    },
                    recipe.identifier
                ).map { it.toIngredientGeneric() },
                (cookStep.cookingTicks / (60 * 20)).toInt()
            )
        val brew = Brew(bIngredients)
        for (i in 1..<recipe.steps.size) {
            val step = recipe.steps[i]
            if (step is DistillStep) {
                brew.distillRuns = step.count.toByte()
            }
            if (step is AgeStep) {
                brew.ageTime = step.agingTicks.toFloat() / (60 * 20)
                brew.wood = BarrelWoodType.valueOf(step.barrelType.name)
            }
        }
        brew.currentRecipe = bIngredients.getBestRecipe(brew.wood, brew.ageTime, brew.distillRuns > 0)
        brew.quality = brew.calcQuality()
        return brew
    }

    private fun convertIngredients(ingredients: List<RecipeItem>): Map<Ingredient, Int> {
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