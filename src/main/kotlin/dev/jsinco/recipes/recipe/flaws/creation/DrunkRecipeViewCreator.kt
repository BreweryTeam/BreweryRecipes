package dev.jsinco.recipes.recipe.flaws.creation

import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.RecipeView
import dev.jsinco.recipes.recipe.flaws.Flaw
import dev.jsinco.recipes.recipe.flaws.FlawConfig
import dev.jsinco.recipes.recipe.flaws.FlawExtent
import dev.jsinco.recipes.recipe.flaws.type.InaccuracyFlawType
import dev.jsinco.recipes.recipe.flaws.type.ReplacementFlawType
import dev.jsinco.recipes.recipe.flaws.type.SlurringFlawType
import kotlin.random.Random

object DrunkRecipeViewCreator : RecipeViewCreator {

    override fun create(breweryRecipe: BreweryRecipe, expectedFlawLevel: Double, random: Random): RecipeView {
        val flaws = mutableListOf<Flaw>()
        if (random.nextBoolean() && expectedFlawLevel > 40) {
            flaws.add(
                Flaw(
                    ReplacementFlawType(".", true),
                    FlawConfig(FlawExtent.compileAfterPoint(breweryRecipe.steps.size, random), random.nextInt(), 100.0)
                )
            )
        }
        flaws.add(
            Flaw(
                SlurringFlawType,
                FlawConfig(FlawExtent.Everywhere, random.nextInt(), expectedFlawLevel)
            )
        )
        flaws.add(
            Flaw(
                InaccuracyFlawType,
                FlawConfig(FlawExtent.Everywhere, random.nextInt(), expectedFlawLevel)
            )
        )
        return RecipeView(breweryRecipe.identifier, flaws)
    }

    override fun createFullyFlawed(breweryRecipe: BreweryRecipe, random: Random): RecipeView {
        return create(breweryRecipe, 100.0, random)
    }

}