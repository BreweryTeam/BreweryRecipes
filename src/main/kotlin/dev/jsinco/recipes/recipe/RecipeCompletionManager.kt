package dev.jsinco.recipes.recipe

import dev.jsinco.recipes.data.PersistencyLinkedCache
import dev.jsinco.recipes.data.storage.StorageImpl
import java.util.*

class RecipeCompletionManager(private val storageImpl: StorageImpl) : PersistencyLinkedCache {

    val backing = mutableMapOf<UUID, MutableMap<String, BreweryRecipe>>()

    fun insertOrUpdateRecipeCompletion(uuid: UUID, recipe: BreweryRecipe) {
        storageImpl.completedRecipeSession()
            .insertOrUpdateRecipeCompletion(uuid, recipe)
        backing.computeIfAbsent(uuid) { mutableMapOf() }[recipe.identifier] = recipe
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
            .thenAccept { completed ->
                completed ?: return@thenAccept
                backing[playerUuid] = completed.associateBy { it.recipeKey() }
                    .toMutableMap()
            }
    }

    fun getCompletedRecipes(playerUuid: UUID): Collection<BreweryRecipe> {
        return backing.getOrDefault(playerUuid, mapOf())
            .values
    }
}