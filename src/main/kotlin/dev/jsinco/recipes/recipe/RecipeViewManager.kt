package dev.jsinco.recipes.recipe

import com.dre.brewery.utility.Logging
import dev.jsinco.recipes.data.StorageImpl
import dev.jsinco.recipes.util.Logger
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class RecipeViewManager(private val storageImpl: StorageImpl) {
    companion object {
        const val CACHE_LIFETIME: Int = 60000 // ms
    }

    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    val backing: MutableMap<UUID, MutableList<RecipeView>> = mutableMapOf()
    val forRemoval: MutableMap<UUID, Long> = mutableMapOf()

    fun initiateViews(playerUuid: UUID) {
        forRemoval.remove(playerUuid)
        if (backing.contains(playerUuid)) {
            return
        }
        storageImpl.selectRecipeViews(playerUuid)
            .thenAcceptAsync({ it?.let { backing[playerUuid] = it.toMutableList() } }, executor)
    }

    fun scheduleViewsUnload(playerUuid: UUID) {
        forRemoval[playerUuid] = System.currentTimeMillis() + CACHE_LIFETIME
    }

    fun tick() {
        val toRemove = forRemoval.filter { it.value < System.currentTimeMillis() }
            .map { it.key }
        toRemove.forEach(forRemoval::remove)
        CompletableFuture.runAsync({
            toRemove.forEach {
                backing.remove(it)
            }
        }, executor)
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