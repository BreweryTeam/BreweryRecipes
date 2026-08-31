package dev.jsinco.recipes.data

import java.util.UUID

interface PersistencyLinkedCache {
    fun initiateCacheFor(playerUuid: UUID)
}