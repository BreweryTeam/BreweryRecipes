package dev.jsinco.recipes.core.flaws.creation

import dev.jsinco.recipes.core.BreweryRecipe
import dev.jsinco.recipes.core.RecipeView
import dev.jsinco.recipes.core.RecipeWriter
import dev.jsinco.recipes.core.flaws.Flaw
import dev.jsinco.recipes.core.flaws.FlawConfig
import dev.jsinco.recipes.core.flaws.FlawExtent
import dev.jsinco.recipes.core.flaws.type.CorrectionFlawType
import dev.jsinco.recipes.core.flaws.type.InaccuracyFlawType
import dev.jsinco.recipes.core.flaws.type.ReplacementFlawType
import kotlin.random.Random

object UncertainRecipeViewCreator : RecipeViewCreator {
    override fun create(breweryRecipe: BreweryRecipe, expectedFlawLevel: Double): RecipeView {
        val arbitraryTargetFlawIntensity = expectedFlawLevel * 0.4
        var flawIntensity = 0.0
        val flaws = mutableListOf<Flaw>()
        while (arbitraryTargetFlawIntensity > flawIntensity) {
            val random1 = Random.nextInt(3)
            val flawType = when (random1) {
                0 -> ReplacementFlawType("?")
                1 -> InaccuracyFlawType
                2 -> CorrectionFlawType
                else -> throw IllegalStateException("Unreachable code, someone messed up")
            }
            val extent = if (random1 == 0 || random1 == 2) {
                if (Random.nextInt(2) == 1) {
                    FlawExtent.compileAfterPoint(breweryRecipe.steps.size)
                } else {
                    FlawExtent.compileStepRange(breweryRecipe.steps.size)
                }
            } else {
                when (Random.nextInt(3)) {
                    0 -> FlawExtent.Everywhere
                    1 -> FlawExtent.compileStepRange(breweryRecipe.steps.size)
                    2 -> FlawExtent.compileWholeStep(breweryRecipe.steps.size)
                    else -> throw IllegalStateException("Unreachable code, someone messed up")
                }
            }
            flaws.add(Flaw(flawType, FlawConfig(extent, Random.nextInt(), expectedFlawLevel)))
            flawIntensity = RecipeWriter.estimateFragmentation(RecipeView.of(breweryRecipe.identifier, flaws))
        }
        return RecipeWriter.clearRedundantFlaws(RecipeView.of(breweryRecipe.identifier, flaws))
    }
}
