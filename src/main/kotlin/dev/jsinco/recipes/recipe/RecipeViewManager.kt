package dev.jsinco.recipes.recipe

import dev.jsinco.recipes.data.PersistencyLinkedCache
import dev.jsinco.recipes.data.storage.StorageImpl
import java.util.*

class RecipeViewManager(private val storageImpl: StorageImpl) : PersistencyLinkedCache {
    companion object {
        const val CACHE_LIFETIME: Int = 60000 // ms
    }

    val backing: MutableMap<UUID, MutableList<RecipeView>> = mutableMapOf()

    override fun initiateCacheFor(playerUuid: UUID) {
        if (backing.contains(playerUuid)) {
            return
        }
        storageImpl.recipeViewSession().selectRecipeViews(playerUuid)
            .thenAccept({
                it?.let {
                    backing[playerUuid] = it.toMutableList()
                }
            })
    }

    fun getViews(playerUuid: UUID): List<RecipeView> {
        return backing[playerUuid] ?: listOf()
    }

    fun insertOrUpdateView(playerUuid: UUID, recipeView: RecipeView) {
        val recipeViews = backing.computeIfAbsent(playerUuid) {
            mutableListOf()
        }
        recipeViews.removeIf { it.recipeIdentifier == recipeView.recipeIdentifier }
        recipeViews.add(recipeView)
        storageImpl.recipeViewSession().insertOrUpdateRecipeView(playerUuid, recipeView)
    }

    fun insertOrMergeView(playerUuid: UUID, incoming: RecipeView) {
        val list = backing.computeIfAbsent(playerUuid) { mutableListOf() }
        val idx = list.indexOfFirst { it.recipeIdentifier == incoming.recipeIdentifier }

        if (idx < 0) {
            val minimalized = RecipeViewLoreWriter.clearRedundantFlaws(incoming)
            list.add(minimalized) // No existing view for this recipe yet, add one
            storageImpl.recipeViewSession().insertOrUpdateRecipeView(playerUuid, minimalized)
            return
        }

        val existing = list[idx]
        val merged = RecipeViewLoreWriter.mergeFlaws(existing, incoming)
        val minimalized = RecipeViewLoreWriter.clearRedundantFlaws(merged)
        list[idx] = minimalized // replace in memory, just to be sure
        storageImpl.recipeViewSession().insertOrUpdateRecipeView(playerUuid, minimalized)
    }

    fun removeView(playerUuid: UUID, recipeKey: String) {
        val recipeViews = backing.computeIfAbsent(playerUuid) {
            mutableListOf()
        }
        recipeViews.removeIf { it.recipeIdentifier == recipeKey }
        storageImpl.recipeViewSession().removeRecipeView(playerUuid, recipeKey)
    }

    fun removeAll(playerUuid: UUID) {
        val views = backing.remove(playerUuid)
        views?.forEach {
            storageImpl.recipeViewSession().removeRecipeView(playerUuid, it.recipeIdentifier)
        }
    }

    override fun clearAll(playerUuid: UUID) {
        val views = backing.remove(playerUuid)
    }
}