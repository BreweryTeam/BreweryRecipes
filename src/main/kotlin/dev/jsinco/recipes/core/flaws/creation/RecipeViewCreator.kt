package dev.jsinco.recipes.core.flaws.creation

import dev.jsinco.recipes.core.BreweryRecipe
import dev.jsinco.recipes.core.RecipeView

interface RecipeViewCreator {

    fun create(breweryRecipe: BreweryRecipe, expectedFlawLevel: Double): RecipeView


    enum class Type(val recipeViewCreator: RecipeViewCreator, val lootTranslationKey: String) {

        ENCRYPTED(EncryptedRecipeViewCreator, "recipes.loot.recipe.encrypted"),
        UNCERTAIN(UncertainRecipeViewCreator, "recipes.loot.recipe.forgetful"),
        DRUNK(DrunkRecipeViewCreator, "recipes.loot.recipe.drunken");
    }
}