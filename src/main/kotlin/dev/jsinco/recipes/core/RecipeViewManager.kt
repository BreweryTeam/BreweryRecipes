package dev.jsinco.recipes.core

import dev.jsinco.recipes.data.StorageImpl
import java.util.*

class RecipeViewManager(private val storageImpl: StorageImpl) {

    val backing: MutableMap<UUID, MutableList<RecipeView>> = mutableMapOf()

    init {
        storageImpl.selectAllRecipeViews()
            .thenAcceptAsync { it?.let { backing.putAll(it) } }
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
        storageImpl.insertOrUpdateRecipeView(playerUuid, recipeView)
    }

    fun insertOrMergeView(playerUuid: UUID, incoming: RecipeView) {
        val list = backing.computeIfAbsent(playerUuid) { mutableListOf() }
        val idx = list.indexOfFirst { it.recipeIdentifier == incoming.recipeIdentifier }

        if (idx < 0) {
            val minimalized = RecipeWriter.clearRedundantFlaws(incoming)
            list.add(minimalized) // No existing view for this recipe yet, add one
            storageImpl.insertOrUpdateRecipeView(playerUuid, minimalized)
            return
        }

        val existing = list[idx]
        val merged = RecipeWriter.mergeFlaws(existing, incoming)
        val minimalized = RecipeWriter.clearRedundantFlaws(merged)
        list[idx] = minimalized // replace in memory, just to be sure
        storageImpl.insertOrUpdateRecipeView(playerUuid, minimalized)
    }

    fun removeView(playerUuid: UUID, recipeKey: String) {
        val recipeViews = backing.computeIfAbsent(playerUuid) {
            mutableListOf()
        }
        recipeViews.removeIf { it.recipeIdentifier == recipeKey }
        storageImpl.removeRecipeView(playerUuid, recipeKey)
    }

    fun clearAll(playerUuid: UUID) {
        val views = backing.remove(playerUuid)
        views?.forEach {
            storageImpl.removeRecipeView(playerUuid, it.recipeIdentifier)
        }
    }
}