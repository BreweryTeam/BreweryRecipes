package dev.jsinco.recipes.configuration.serialize

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.spawning.BlockDropSpawnConfig
import dev.jsinco.recipes.spawning.LootSpawnConfig
import dev.jsinco.recipes.spawning.MobDropSpawnConfig
import dev.jsinco.recipes.spawning.SpawnConfig
import dev.jsinco.recipes.spawning.conditions.BiomeCondition
import dev.jsinco.recipes.spawning.conditions.SpawnCondition
import dev.jsinco.recipes.spawning.conditions.WorldCondition
import dev.jsinco.recipes.util.Logger
import eu.okaeri.configs.schema.GenericsDeclaration
import eu.okaeri.configs.serdes.DeserializationData
import eu.okaeri.configs.serdes.ObjectSerializer
import eu.okaeri.configs.serdes.SerializationData
import org.bukkit.NamespacedKey
import org.bukkit.loot.LootTable

object SpawnConfigSerializer : ObjectSerializer<SpawnConfig> {

    override fun supports(type: Class<in SpawnConfig>): Boolean {
        return SpawnConfig::class.java.isAssignableFrom(type)
    }

    override fun serialize(
        `object`: SpawnConfig,
        data: SerializationData,
        generics: GenericsDeclaration
    ) {
        data.add("type", `object`.type.name)

        `object`.enabled?.let { data.add("enabled", it) }
        `object`.attempts?.let { data.add("attempts", it) }
        `object`.chance?.let { data.add("chance", it) }

        if (!`object`.whitelist.isNullOrEmpty()) data.add("whitelist", `object`.whitelist)
        if (!`object`.blacklist.isNullOrEmpty()) data.add("blacklist", `object`.blacklist)

        if (!`object`.conditions.isNullOrEmpty()) {
            val serializedConditions = `object`.conditions.mapNotNull {
                when (it) {
                    is BiomeCondition -> it.toMap()
                    is WorldCondition -> it.toMap()
                    else -> null
                }
            }
            if (serializedConditions.isNotEmpty()) data.add("conditions", serializedConditions)
        }

        when (`object`) {
            is LootSpawnConfig -> {
                if (`object`.lootTables.isNotEmpty()) {
                    val lootTableKeys = `object`.lootTables.mapNotNull { it.key?.asString() }
                    if (lootTableKeys.isNotEmpty()) data.add("lootTables", lootTableKeys)
                }
            }
            is MobDropSpawnConfig -> {
                if (`object`.entities.isNotEmpty()) data.add("entities", `object`.entities)
            }
            is BlockDropSpawnConfig -> {
                if (`object`.blocks.isNotEmpty()) data.add("blocks", `object`.blocks)
            }
        }
    }

    override fun deserialize(
        data: DeserializationData,
        generics: GenericsDeclaration
    ): SpawnConfig? {
        val typeName = data.get("type", String::class.java) ?: return null
        val type = try {
            SpawnConfig.SpawnConfigType.valueOf(typeName.uppercase())
        } catch (ex: IllegalArgumentException) {
            Logger.logErr("Unknown SpawnConfigType: $typeName")
            return null
        }

        val enabled = data.get("enabled", Boolean::class.javaObjectType)
        val attempts = data.get("attempts", Int::class.javaObjectType)
        val chance = data.get("chance", Double::class.javaObjectType)
        val whitelist = data.getAsList("whitelist", String::class.java) ?: listOf()
        val blacklist = data.getAsList("blacklist", String::class.java) ?: listOf()

        @Suppress("UNCHECKED_CAST") // No custom condition serializer, as new conditions might look different
        val rawConditions = data.get("conditions", Any::class.java) as? List<Map<String, Any>> ?: listOf()
        val conditions = mutableListOf<SpawnCondition>()
        for (rawCondition in rawConditions) {
            val type = (rawCondition["type"] as? String)?.lowercase() ?: continue
            when (type) {
                "biome" -> conditions.add(BiomeCondition.fromMap(rawCondition))
                "world" -> conditions.add(WorldCondition.fromMap(rawCondition))
                else -> Logger.logErr("Unknown condition type: $type")
            }
        }

        return when (type) {
            SpawnConfig.SpawnConfigType.LOOT -> {
                val lootTableStrings = data.getAsList("lootTables", String::class.java) ?: listOf()
                val lootTables = mutableListOf<LootTable>()

                for (string in lootTableStrings) {
                    val key = NamespacedKey.fromString(string)
                    if (key == null) {
                        Logger.logErr("Couldn't parse NamespacedKey from string: $string")
                        continue
                    }
                    val lootTable = Recipes.instance.server.getLootTable(key)
                    if (lootTable == null) {
                        Logger.logErr("Couldn't find LootTable: ${key.asString()}")
                        continue
                    }
                    lootTables.add(lootTable)
                }

                LootSpawnConfig(
                    type = type,
                    enabled = enabled,
                    attempts = attempts,
                    chance = chance,
                    whitelist = whitelist.takeIf { it.isNotEmpty() },
                    blacklist = blacklist.takeIf { it.isNotEmpty() },
                    conditions = conditions.takeIf { it.isNotEmpty() },
                    lootTables = lootTables
                )
            }

            SpawnConfig.SpawnConfigType.MOB_DROP -> {
                val entities = data.getAsList("entities", String::class.java) ?: listOf()
                MobDropSpawnConfig(
                    enabled = enabled,
                    attempts = attempts,
                    chance = chance,
                    whitelist = whitelist.takeIf { it.isNotEmpty() },
                    blacklist = blacklist.takeIf { it.isNotEmpty() },
                    conditions = conditions.takeIf { it.isNotEmpty() },
                    entities = entities.toMutableList()
                )
            }

            SpawnConfig.SpawnConfigType.BLOCK_DROP -> {
                val blocks = data.getAsList("blocks", String::class.java) ?: listOf()
                BlockDropSpawnConfig(
                    enabled = enabled,
                    attempts = attempts,
                    chance = chance,
                    whitelist = whitelist.takeIf { it.isNotEmpty() },
                    blacklist = blacklist.takeIf { it.isNotEmpty() },
                    conditions = conditions.takeIf { it.isNotEmpty() },
                    blocks = blocks.toMutableList()
                )
            }

            else -> SpawnConfig(
                type = type,
                enabled = enabled,
                attempts = attempts,
                chance = chance,
                whitelist = whitelist.takeIf { it.isNotEmpty() },
                blacklist = blacklist.takeIf { it.isNotEmpty() },
                conditions = conditions.takeIf { it.isNotEmpty() }
            )
        }
    }
}