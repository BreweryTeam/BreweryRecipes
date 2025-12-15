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
    override fun create(breweryRecipe: BreweryRecipe, expectedFlawLevel: Double): RecipeView {
        val flaws = mutableListOf<Flaw>()
        if (Random.nextBoolean() && expectedFlawLevel > 40) {
            flaws.add(
                Flaw(
                    ReplacementFlawType(".", true),
                    FlawConfig(FlawExtent.compileAfterPoint(breweryRecipe.steps.size), Random.nextInt(), 100.0)
                )
            )
        }
        flaws.add(
            Flaw(
                SlurringFlawType,
                FlawConfig(FlawExtent.Everywhere, Random.nextInt(), expectedFlawLevel)
            )
        )
        flaws.add(
            Flaw(
                InaccuracyFlawType,
                FlawConfig(FlawExtent.Everywhere, Random.nextInt(), expectedFlawLevel)
            )
        )
        return RecipeView.of(breweryRecipe.identifier, flaws)
    }
}