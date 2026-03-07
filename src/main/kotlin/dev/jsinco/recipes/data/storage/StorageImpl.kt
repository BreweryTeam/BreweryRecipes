package dev.jsinco.recipes.data.storage

import dev.jsinco.recipes.data.StorageType

interface StorageImpl {

    fun getType(): StorageType

    fun recipeViewSession(): RecipeViewStorageSession

    fun completedRecipeSession(): CompletedRecipeStorageSession

    fun createTables()

}