package dev.jsinco.recipes.recipe.flaws.creation

import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.RecipeView
import kotlin.random.Random

interface RecipeViewCreator {

    fun create(breweryRecipe: BreweryRecipe, expectedFlawLevel: Double, random: Random = Random.Default): RecipeView

    fun createFullyFlawed(breweryRecipe: BreweryRecipe, random: Random = Random.Default): RecipeView

    enum class Type(val recipeViewCreator: RecipeViewCreator, val lootTranslationKey: String) {

        ENCRYPTED(EncryptedRecipeViewCreator, "breweryrecipes.spawning.item.name.encrypted"),
        UNCERTAIN(UncertainRecipeViewCreator, "breweryrecipes.spawning.item.name.forgetful"),
        DRUNK(DrunkRecipeViewCreator, "breweryrecipes.spawning.item.name.drunken");
    }
}