package dev.jsinco.recipes.configuration.spawning.triggers

import org.bukkit.entity.EntityType
import org.bukkit.event.inventory.InventoryType

enum class PremadeTrigger(val spawnTrigger: SpawnTrigger? = null) {

    CHEST(InventoryFillTrigger(InventoryType.CHEST)),
    BARREL(InventoryFillTrigger(InventoryType.BARREL)),
    MINECART,
    FISHING,

}