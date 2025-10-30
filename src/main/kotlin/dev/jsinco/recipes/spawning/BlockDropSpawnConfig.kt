package dev.jsinco.recipes.spawning

import dev.jsinco.recipes.spawning.conditions.SpawnCondition
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey

class BlockDropSpawnConfig(
    enabled: Boolean? = null,
    attempts: Int? = null,
    chance: Double? = null,
    whitelist: List<String>? = null,
    blacklist: List<String>? = null,
    conditions: List<SpawnCondition>? = null,
    val blocks: MutableList<String> = mutableListOf(),
) : SpawnConfig(SpawnConfigType.BLOCK_DROP, enabled, attempts, chance, whitelist, blacklist, conditions) {

    fun contains(type: Material): Boolean {
        for (entry in blocks) {
            Material.entries.firstOrNull { it.name.equals(entry, ignoreCase = true) }?.let {
                if (it == type) return true
            }

            var keyString = entry.removePrefix("#")
            keyString = keyString.removePrefix("minecraft:")
            val tag = Bukkit.getTag("blocks", NamespacedKey.minecraft(keyString), Material::class.java)
            if (tag?.isTagged(type) ?: false) {
                return true
            }
        }
        return false
    }

    class Builder : SpawnConfig.Builder(SpawnConfigType.BLOCK_DROP) {
        private val blocks: MutableList<String> = mutableListOf()

        fun setBlocks(list: MutableList<String>) = apply { blocks.clear(); blocks.addAll(list) }
        fun addBlock(nameOrTag: String) = apply { blocks.add(nameOrTag) }

        override fun build(): BlockDropSpawnConfig {
            return BlockDropSpawnConfig(
                enabled = enabled,
                attempts = attempts,
                chance = chance,
                whitelist = if (whitelist.isEmpty()) null else whitelist,
                blacklist = if (blacklist.isEmpty()) null else blacklist,
                conditions = if (conditions.isEmpty()) null else conditions,
                blocks = blocks
            )
        }
    }

    companion object {
        fun builder() = Builder()
    }
}
