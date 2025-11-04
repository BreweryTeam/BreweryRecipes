package dev.jsinco.recipes.configuration.spawning.triggers

import org.bukkit.event.inventory.InventoryType

enum class PremadeTrigger(val spawnTrigger: SpawnTrigger) {

    CHEST(InventoryFillTrigger(InventoryType.CHEST)),
    BARREL(InventoryFillTrigger(InventoryType.BARREL)),
    MINECART(LootSpawnTrigger.fromStrings("chests/abandoned_mineshaft")),
    FISHING(LootSpawnTrigger.fromStrings("gameplay/fishing/treasure")),
    
}