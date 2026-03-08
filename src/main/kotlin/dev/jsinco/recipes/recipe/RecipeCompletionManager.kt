package dev.jsinco.recipes.recipe

import dev.jsinco.recipes.data.PersistencyLinkedCache
import dev.jsinco.recipes.data.storage.StorageImpl
import dev.jsinco.recipes.recipe.process.Recipe
import java.util.*

class RecipeCompletionManager(private val storageImpl: StorageImpl) : PersistencyLinkedCache {

    val backing = mutableMapOf<UUID, MutableMap<String, Recipe>>()

    fun insertOrUpdateRecipeCompletion(uuid: UUID, recipe: Recipe) {
        storageImpl.completedRecipeSession()
            .insertOrUpdateRecipeCompletion(uuid, recipe)
        backing.computeIfAbsent(uuid) { mutableMapOf() }[recipe.recipeKey] = recipe
    }

    fun removeCompletion(uuid: UUID, recipeKey: String) {
        storageImpl.completedRecipeSession()
            .removeRecipeCompletion(uuid, recipeKey)
        backing[uuid]?.remove(recipeKey)
    }

    override fun clearAll(playerUuid: UUID) {
        backing.remove(playerUuid)
    }

    override fun initiateCacheFor(playerUuid: UUID) {
        storageImpl.completedRecipeSession()
            .selectRecipeCompletions(playerUuid)
    }

    fun getCompletedRecipes(playerUuid: UUID): Collection<Recipe> {
        return backing.getOrDefault(playerUuid, mapOf())
            .values
    }
}