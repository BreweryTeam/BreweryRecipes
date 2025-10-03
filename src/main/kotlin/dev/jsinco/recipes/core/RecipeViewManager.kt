package dev.jsinco.recipes.core

import dev.jsinco.recipes.core.flaws.Flaw
import dev.jsinco.recipes.core.flaws.FlawBundle
import dev.jsinco.recipes.core.flaws.FlawConfig
import dev.jsinco.recipes.core.flaws.FlawType
import dev.jsinco.recipes.core.flaws.number.InaccuracyFlawType
import dev.jsinco.recipes.core.flaws.text.AmnesiaFlawType
import dev.jsinco.recipes.core.flaws.text.ObfuscationFlawType
import dev.jsinco.recipes.core.flaws.text.OmissionFlawType
import dev.jsinco.recipes.core.flaws.text.SlurringFlawType
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
            list.add(incoming) // No existing view for this recipe yet, add one
            storageImpl.insertOrUpdateRecipeView(playerUuid, incoming)
            return
        }

        val existing = list[idx]
        val mergedBundles = dedupeBundles(existing.flaws + incoming.flaws)
        val merged = RecipeView(existing.recipeIdentifier, mergedBundles)
        val normalizedView = RecipeWriter.normalizeFlawsIfLowFragmentation(merged)
        list[idx] = normalizedView // replace in memory, just to be sure
        storageImpl.insertOrUpdateRecipeView(playerUuid, normalizedView)
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
        is AmnesiaFlawType -> "amnesia"
        is ObfuscationFlawType -> "obfuscation"
        is OmissionFlawType -> "omission"
        is SlurringFlawType -> "slurring"
        else -> ft::class.qualifiedName ?: ft::class.simpleName ?: "unknown"
    }
}