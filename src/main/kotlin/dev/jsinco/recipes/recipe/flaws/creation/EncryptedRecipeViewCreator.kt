package dev.jsinco.recipes.recipe.flaws.creation

import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.RecipeView
import dev.jsinco.recipes.recipe.RecipeWriter
import dev.jsinco.recipes.recipe.flaws.Flaw
import dev.jsinco.recipes.recipe.flaws.FlawConfig
import dev.jsinco.recipes.recipe.flaws.FlawExtent
import dev.jsinco.recipes.recipe.flaws.type.ObfuscationFlawType
import dev.jsinco.recipes.recipe.flaws.type.ReplacementFlawType
import kotlin.random.Random

object EncryptedRecipeViewCreator : RecipeViewCreator {
    override fun create(breweryRecipe: BreweryRecipe, expectedFlawLevel: Double): RecipeView {
        var flawFragmentation = 0.0
        val flaws = mutableListOf<Flaw>()
        val arbitraryTargetFlawIntensity = expectedFlawLevel * 0.8
        var maximumLoops = 10
        while (arbitraryTargetFlawIntensity > flawFragmentation && maximumLoops-- > 0) {
            val flawType = when (Random.nextInt(3)) {
                0 -> ObfuscationFlawType
                1 -> ReplacementFlawType(" ")
                2 -> ReplacementFlawType("  ")
                else -> throw IllegalStateException("Unreachable code, someone messed up")
            }
            flaws.add(Flaw(flawType, FlawConfig(FlawExtent.Everywhere, Random.nextInt(), expectedFlawLevel * 2 / 3)))
            flawFragmentation =
                RecipeWriter.estimateFragmentation(RecipeView.of(breweryRecipe.identifier, flaws))
        }
        return RecipeWriter.clearRedundantFlaws(RecipeView.of(breweryRecipe.identifier, flaws))
    }
}