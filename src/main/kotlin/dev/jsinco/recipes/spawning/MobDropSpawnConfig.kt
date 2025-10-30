package dev.jsinco.recipes.spawning

import dev.jsinco.recipes.spawning.conditions.SpawnCondition
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType

class MobDropSpawnConfig(
    enabled: Boolean? = null,
    attempts: Int? = null,
    chance: Double? = null,
    whitelist: List<String>? = null,
    blacklist: List<String>? = null,
    conditions: List<SpawnCondition>? = null,
    val entities: MutableList<String> = mutableListOf()
) : SpawnConfig(SpawnConfigType.MOB_DROP, enabled, attempts, chance, whitelist, blacklist, conditions) {

    fun contains(type: EntityType): Boolean {
        for (entry in entities) {
            EntityType.entries.firstOrNull { it.name.equals(entry, ignoreCase = true) }?.let {
                if (it == type) return true
            }

            var keyString = entry.removePrefix("#")
            keyString = keyString.removePrefix("minecraft:")
            val tag = Bukkit.getTag("entity_types", NamespacedKey.minecraft(keyString), EntityType::class.java)
            if (tag?.isTagged(type) ?: false) {
                return true
            }
        }
        return false
    }

    class Builder : SpawnConfig.Builder(SpawnConfigType.MOB_DROP) {
        private val entities: MutableList<String> = mutableListOf()

        fun setEntities(list: MutableList<String>) = apply { entities.clear(); entities.addAll(list) }
        fun addEntity(nameOrTag: String) = apply { entities.add(nameOrTag) }

        override fun build(): MobDropSpawnConfig {
            return MobDropSpawnConfig(
                enabled = enabled,
                attempts = attempts,
                chance = chance,
                whitelist = if (whitelist.isEmpty()) null else whitelist,
                blacklist = if (blacklist.isEmpty()) null else blacklist,
                conditions = if (conditions.isEmpty()) null else conditions,
                entities = entities
            )
        }
    }

    companion object {
        fun builder() = Builder()
    }
}
