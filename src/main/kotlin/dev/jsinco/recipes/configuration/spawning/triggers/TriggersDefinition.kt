package dev.jsinco.recipes.configuration.spawning.triggers

data class TriggersDefinition(
    val blockDropTrigger: BlockDropTrigger? = null,
    val inventoryFillTrigger: InventoryFillTrigger? = null,
    val lootSpawnTrigger: LootSpawnTrigger? = null,
    val mobDropTrigger: MobDropTrigger? = null,
    val premadeTrigger: List<PremadeTrigger>? = null,
) {
    fun asList(): List<SpawnTrigger> {
        return buildList {
            blockDropTrigger?.let { add(it) }
            inventoryFillTrigger?.let { add(it) }
            lootSpawnTrigger?.let { add(it) }
            mobDropTrigger?.let { add(it) }
            premadeTrigger?.let {
                addAll(it.mapNotNull(PremadeTrigger::spawnTrigger))
            }
        }
    }
}
