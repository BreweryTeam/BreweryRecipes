package dev.jsinco.recipes.recipe.flaws.creation

import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.RecipeView

interface RecipeViewCreator {

    fun create(breweryRecipe: BreweryRecipe, expectedFlawLevel: Double): RecipeView


    enum class Type(val recipeViewCreator: RecipeViewCreator, val lootTranslationKey: String) {

        ENCRYPTED(EncryptedRecipeViewCreator, "spawning.item.name.encrypted"),
        UNCERTAIN(UncertainRecipeViewCreator, "spawning.item.name.forgetful"),
        DRUNK(DrunkRecipeViewCreator, "spawning.item.name.drunken");
    }
}