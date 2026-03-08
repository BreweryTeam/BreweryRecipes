package dev.jsinco.recipes.data

import java.util.UUID

interface PersistencyLinkedCache {

    fun clearAll(playerUuid: UUID)

    fun initiateCacheFor(playerUuid: UUID)
}