package dev.jsinco.recipes.core.flaws.creation

import dev.jsinco.recipes.core.BreweryRecipe
import dev.jsinco.recipes.core.RecipeView
import dev.jsinco.recipes.core.RecipeWriter
import dev.jsinco.recipes.core.flaws.Flaw
import dev.jsinco.recipes.core.flaws.FlawConfig
import dev.jsinco.recipes.core.flaws.FlawExtent
import dev.jsinco.recipes.core.flaws.type.ObfuscationFlawType
import dev.jsinco.recipes.core.flaws.type.ReplacementFlawType
import kotlin.random.Random

object EncryptedRecipeViewCreator : RecipeViewCreator {
    override fun create(breweryRecipe: BreweryRecipe, expectedFlawLevel: Double): RecipeView {
        var flawFragmentation = 0.0
        val flaws = mutableListOf<Flaw>()
        val arbitraryTargetFlawIntensity = expectedFlawLevel * 0.8
        while (arbitraryTargetFlawIntensity > flawFragmentation) {
            val flawType = when (Random.nextInt(3)) {
                0 -> ObfuscationFlawType
                1 -> ReplacementFlawType(" ")
                2 -> ReplacementFlawType("  ")
                else -> throw IllegalStateException("Unreachable code, someone messed up")
            }
            flaws.add(Flaw(flawType, FlawConfig(FlawExtent.Everywhere, Random.nextInt(), expectedFlawLevel)))
            flawFragmentation =
                RecipeWriter.estimateFragmentation(RecipeView.of(breweryRecipe.identifier, flaws))
        }
        return RecipeWriter.clearRedundantFlaws(RecipeView.of(breweryRecipe.identifier, flaws))
    }
}