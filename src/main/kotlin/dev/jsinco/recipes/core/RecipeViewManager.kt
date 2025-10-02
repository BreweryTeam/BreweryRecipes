package dev.jsinco.recipes.core

import dev.jsinco.recipes.data.StorageImpl
import java.util.*

class RecipeViewManager(private val storageImpl: StorageImpl) {

    val backing: MutableMap<UUID, MutableList<RecipeView>> = mutableMapOf()

    init {
        storageImpl.selectAllRecipeViews()
            .thenAcceptAsync { backing.putAll(it) }
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

    fun removeView(playerUuid: UUID, recipeKey: String) {
        val recipeViews = backing.computeIfAbsent(playerUuid) {
            mutableListOf()
        }
        recipeViews.removeIf { it.recipeIdentifier == recipeKey }
        storageImpl.removeRecipeView(playerUuid, recipeKey)
    }
}