package dev.jsinco.recipes.core.flaws.creation

import dev.jsinco.recipes.core.BreweryRecipe
import dev.jsinco.recipes.core.RecipeView

interface RecipeViewCreator {

    fun create(breweryRecipe: BreweryRecipe, expectedFlawLevel: Double): RecipeView


    enum class Type(val recipeViewCreator: RecipeViewCreator) {

        ENCRYPTED(EncryptedRecipeViewCreator),
        UNCERTAIN(UncertainRecipeViewCreator),
        DRUNK(DrunkRecipeViewCreator);
    }
}