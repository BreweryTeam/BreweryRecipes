package dev.jsinco.recipes.configuration.spawning.triggers

data class TriggersDefinition(
    val blockDropTrigger: BlockDropTrigger? = null,
    val inventoryFillTrigger: InventoryFillTrigger? = null,
    val lootSpawnTrigger: LootSpawnTrigger? = null,
    val mobDropTrigger: MobDropTrigger? = null,
)
