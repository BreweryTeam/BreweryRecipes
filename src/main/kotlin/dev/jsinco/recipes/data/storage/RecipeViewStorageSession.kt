package dev.jsinco.recipes.data.storage

import dev.jsinco.recipes.recipe.RecipeView
import java.util.UUID
import java.util.concurrent.CompletableFuture

interface RecipeViewStorageSession {
    fun insertOrUpdateRecipeView(playerUuid: UUID, recipeView: RecipeView): CompletableFuture<Void?>

    fun removeRecipeView(playerUuid: UUID, recipeKey: String): CompletableFuture<Void?>

    fun selectRecipeViews(playerUuid: UUID): CompletableFuture<List<RecipeView>?>

}