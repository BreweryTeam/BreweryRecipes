package dev.jsinco.recipes.configuration.spawning.triggers

import dev.jsinco.recipes.util.Logger
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.loot.LootTable

class LootSpawnTrigger(
    val lootTables: List<LootTable> = listOf()
) : SpawnTrigger {


    companion object {

        fun fromStrings(vararg lootTableStrings: String): LootSpawnTrigger {
            return LootSpawnTrigger(
                lootTableStrings
                    .mapNotNull(LootSpawnTrigger::fromString)
            )
        }

        private fun fromString(lootTableString: String): LootTable? {
            val key: NamespacedKey? = NamespacedKey.fromString(lootTableString)
            if (key == null) {
                Logger.logErr("Couldn't parse NamespacedKey from string: $lootTableString")
                return null
            }
            val temp = Bukkit.getLootTable(key)
            temp ?: {
                Logger.logErr("Unknown loot table: $lootTableString")
            }
            return temp
        }
    }
}