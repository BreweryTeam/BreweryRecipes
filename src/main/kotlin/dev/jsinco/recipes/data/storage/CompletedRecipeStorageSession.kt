package dev.jsinco.recipes.data.storage

import dev.jsinco.recipes.recipe.BreweryRecipe
import java.util.*
import java.util.concurrent.CompletableFuture

interface CompletedRecipeStorageSession {

    fun insertOrUpdateRecipeCompletion(playerUuid: UUID, recipe: BreweryRecipe): CompletableFuture<Void?>

    fun removeRecipeCompletion(playerUuid: UUID, recipeKey: String): CompletableFuture<Void?>

    fun selectRecipeCompletions(playerUuid: UUID): CompletableFuture<List<BreweryRecipe>?>

}