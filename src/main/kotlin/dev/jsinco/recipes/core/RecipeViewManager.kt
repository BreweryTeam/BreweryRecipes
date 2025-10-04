package dev.jsinco.recipes.core

import dev.jsinco.recipes.core.flaws.Flaw
import dev.jsinco.recipes.core.flaws.FlawBundle
import dev.jsinco.recipes.core.flaws.FlawConfig
import dev.jsinco.recipes.core.flaws.type.*
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
        val mergedBundles = dedupeBundles(existing.flaws + incoming.flaws)
        val merged = RecipeView(existing.recipeIdentifier, mergedBundles)
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

    // Prevent duplicate bundles, even if we mess up somewhere
    private fun dedupeBundles(bundles: List<FlawBundle>): List<FlawBundle> {
        if (bundles.isEmpty()) return bundles
        val seen = HashSet<Set<FlawKey>>()
        val out = ArrayList<FlawBundle>(bundles.size)
        for (b in bundles) {
            val key = b.flaws.map { flawKey(it) }.toSet()
            if (seen.add(key)) {
                out += b
            }
        }
        return out
    }

    private data class FlawKey(val typeId: String, val config: FlawConfig)

    private fun flawKey(f: Flaw): FlawKey = FlawKey(typeId(f.type), f.config)

    private fun typeId(ft: FlawType): String = when (ft) {
        is InaccuracyFlawType -> "inaccuracy"
        is ReplacementFlawType -> "replacement.${ft.replacement}"
        is ObfuscationFlawType -> "obfuscation"
        is SlurringFlawType -> "slurring"
        else -> ft::class.qualifiedName ?: ft::class.simpleName ?: "unknown"
    }
}