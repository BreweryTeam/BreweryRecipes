package dev.jsinco.recipes.core.flaws.creation

import dev.jsinco.recipes.core.BreweryRecipe
import dev.jsinco.recipes.core.RecipeView
import dev.jsinco.recipes.core.flaws.Flaw
import dev.jsinco.recipes.core.flaws.FlawConfig
import dev.jsinco.recipes.core.flaws.FlawExtent
import dev.jsinco.recipes.core.flaws.type.ReplacementFlawType
import kotlin.random.Random

object DrunkRecipeViewCreator : RecipeViewCreator {
    override fun create(breweryRecipe: BreweryRecipe, expectedFlawLevel: Double): RecipeView {
        var flawFragmentation = 0.0
        val flaws = mutableListOf<Flaw>()
        val arbitraryTargetFlawIntensity = expectedFlawLevel * 0.8
        if (Random.nextBoolean()) {
            flaws.add(
                Flaw(
                    ReplacementFlawType("?"),
                    FlawConfig(FlawExtent.compileAfterPoint(breweryRecipe.steps.size), Random.nextInt(), 100.0)
                )
            )
        }
        // TODO complete this
        return RecipeView.of(breweryRecipe.identifier, flaws)
    }
}