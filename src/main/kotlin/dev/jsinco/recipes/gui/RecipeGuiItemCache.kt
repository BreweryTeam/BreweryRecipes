package dev.jsinco.recipes.gui

import dev.jsinco.recipes.data.PersistencyLinkedCache
import java.util.Optional
import java.util.UUID

class RecipeGuiItemCache : PersistencyLinkedCache {

    private val playerCache: MutableMap<UUID, MutableMap<String, Optional<GuiItem>>> = mutableMapOf()
    private val adminCache: MutableMap<String, Optional<GuiItem>> = mutableMapOf()

    fun resolve(
        playerUuid: UUID,
        recipeIdentifier: String,
        admin: Boolean,
        mode: RecipeBookMode,
        builder: () -> GuiItem?
    ): GuiItem? {
        val cacheKey = "${mode.identifier()}:$recipeIdentifier"
        val target = if (admin) {
            adminCache
        } else {
            playerCache.getOrPut(playerUuid) { mutableMapOf() }
        }
        return target.getOrPut(cacheKey) { Optional.ofNullable(builder()) }.orElse(null)
    }

    fun invalidate(playerUuid: UUID, recipeIdentifier: String) {
        val playerMap = playerCache[playerUuid] ?: return
        RecipeBookMode.entries.forEach { mode ->
            playerMap.remove("${mode.identifier()}:$recipeIdentifier")
        }
    }

    fun clearGlobal() {
        playerCache.clear()
        adminCache.clear()
    }

    override fun clearAll(playerUuid: UUID) {
        playerCache.remove(playerUuid)
    }

    override fun initiateCacheFor(playerUuid: UUID) {
        // lazy - nothing to init
    }
}
