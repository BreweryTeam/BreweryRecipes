package dev.jsinco.recipes.spawning

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.spawning.conditions.SpawnCondition
import dev.jsinco.recipes.util.Logger
import org.bukkit.NamespacedKey
import org.bukkit.loot.LootTable

class LootSpawnConfig(
    type: SpawnConfigType,
    enabled: Boolean? = null,
    attempts: Int? = null,
    chance: Double? = null,
    whitelist: List<String>? = null,
    blacklist: List<String>? = null,
    conditions: List<SpawnCondition>? = null,
    val lootTables: MutableList<LootTable> = mutableListOf()
) : SpawnConfig(type, enabled, attempts, chance, whitelist, blacklist, conditions) {

    class Builder() : SpawnConfig.Builder(SpawnConfigType.LOOT) {
        private var lootTables: MutableList<LootTable> = mutableListOf()

        fun setLootTables(lootTables: MutableList<LootTable>) = apply { this.lootTables = lootTables }
        fun addLootTable(lootTable: LootTable) = apply { this.lootTables.add(lootTable) }
        fun setLootTablesAsStrings(strings: MutableList<String>) = apply {
            this.lootTables.clear()
            for (string in strings) this.addLootTableAsString(string)
        }
        fun addLootTableAsString(string: String) = apply {
            val key: NamespacedKey? = NamespacedKey.fromString(string)
            if (key == null) {
                Logger.logErr("Couldn't parse NamespacedKey from string: $string")
                return@apply
            }
            val lootTable: LootTable? = Recipes.instance.server.getLootTable(key)
            if (lootTable == null) Logger.logErr("Couldn't find LootTable: ${key.asString()}")
            else this.lootTables.add(lootTable)
        }

        override fun build(): LootSpawnConfig {
            return LootSpawnConfig(
                type,
                enabled,
                attempts,
                chance,
                if (whitelist.isEmpty()) null else whitelist,
                if (blacklist.isEmpty()) null else blacklist,
                if (conditions.isEmpty()) null else conditions,
                lootTables
            )
        }
    }

    companion object {
        fun builder(type: SpawnConfigType) = Builder(type)
    }
}