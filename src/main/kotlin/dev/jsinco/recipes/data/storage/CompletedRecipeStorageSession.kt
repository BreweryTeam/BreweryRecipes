package dev.jsinco.recipes.data.storage

import dev.jsinco.recipes.recipe.RecipeView
import dev.jsinco.recipes.recipe.process.Recipe
import java.util.*
import java.util.concurrent.CompletableFuture

interface CompletedRecipeStorageSession {

    fun insertOrUpdateRecipeCompletion(playerUuid: UUID, recipe: Recipe): CompletableFuture<Void?>

    fun removeRecipeCompletion(playerUuid: UUID, recipeKey: String): CompletableFuture<Void?>

    fun selectRecipeCompletions(playerUuid: UUID): CompletableFuture<List<Recipe>?>

}