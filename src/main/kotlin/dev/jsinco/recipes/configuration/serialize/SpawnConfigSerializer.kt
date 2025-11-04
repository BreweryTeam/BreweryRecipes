package dev.jsinco.recipes.configuration.serialize

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.core.flaws.creation.RecipeViewCreator
import dev.jsinco.recipes.configuration.spawning.triggers.BlockDropTrigger
import dev.jsinco.recipes.configuration.spawning.triggers.LootSpawnTrigger
import dev.jsinco.recipes.configuration.spawning.triggers.MobDropTrigger
import dev.jsinco.recipes.configuration.spawning.SpawnDefinition
import dev.jsinco.recipes.configuration.spawning.conditions.BiomeCondition
import dev.jsinco.recipes.configuration.spawning.conditions.SpawnCondition
import dev.jsinco.recipes.configuration.spawning.conditions.WorldCondition
import dev.jsinco.recipes.util.Logger
import eu.okaeri.configs.schema.GenericsDeclaration
import eu.okaeri.configs.serdes.DeserializationData
import eu.okaeri.configs.serdes.ObjectSerializer
import eu.okaeri.configs.serdes.SerializationData
import org.bukkit.NamespacedKey
import org.bukkit.loot.LootTable

object SpawnConfigSerializer : ObjectSerializer<SpawnDefinition> {

    override fun supports(type: Class<in SpawnDefinition>): Boolean {
        return SpawnDefinition::class.java.isAssignableFrom(type)
    }

    override fun serialize(
        `object`: SpawnDefinition,
        data: SerializationData,
        generics: GenericsDeclaration
    ) {
        data.add("type", `object`.type.name)

        `object`.enabled?.let { data.add("enabled", it) }
        `object`.attempts?.let { data.add("attempts", it) }
        `object`.chance?.let { data.add("chance", it) }

        if (!`object`.whitelist.isNullOrEmpty()) data.add("whitelist", `object`.whitelist)
        if (!`object`.blacklist.isNullOrEmpty()) data.add("blacklist", `object`.blacklist)
        data.add("flawless", `object`.flawless)
        `object`.flaw ?: let {
            data.add("flaw", it)
        }

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
            is LootSpawnTrigger -> {
                if (`object`.lootTables.isNotEmpty()) {
                    val lootTableKeys = `object`.lootTables.mapNotNull { it.key?.asString() }
                    if (lootTableKeys.isNotEmpty()) data.add("lootTables", lootTableKeys)
                }
            }
            is MobDropTrigger -> {
                if (`object`.entities.isNotEmpty()) data.add("entities", `object`.entities)
            }
            is BlockDropTrigger -> {
                if (`object`.blocks.isNotEmpty()) data.add("blocks", `object`.blocks)
            }
        }
    }

    override fun deserialize(
        data: DeserializationData,
        generics: GenericsDeclaration
    ): SpawnDefinition? {
        val typeName = data.get("type", String::class.java) ?: return null
        val type = try {
            SpawnDefinition.SpawnConfigType.valueOf(typeName.uppercase())
        } catch (_: IllegalArgumentException) {
            Logger.logErr("Unknown SpawnConfigType: $typeName")
            return null
        }

        val enabled = data.get("enabled", Boolean::class.java) ?: true
        val attempts = data.get("attempts", Int::class.javaObjectType)
        val chance = data.get("chance", Double::class.javaObjectType)
        val whitelist = data.getAsList("whitelist", String::class.java) ?: listOf()
        val blacklist = data.getAsList("blacklist", String::class.java) ?: listOf()
        val flawless = data.get("flawless", Boolean::class.java) ?: false
        val flaw = data.get("flaw", RecipeViewCreator.Type::class.java)

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
            SpawnDefinition.SpawnConfigType.LOOT -> {
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

                LootSpawnTrigger(
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

            SpawnDefinition.SpawnConfigType.MOB_DROP -> {
                val entities = data.getAsList("entities", String::class.java) ?: listOf()
                MobDropTrigger(
                    enabled = enabled,
                    attempts = attempts,
                    chance = chance,
                    whitelist = whitelist.takeIf { it.isNotEmpty() },
                    blacklist = blacklist.takeIf { it.isNotEmpty() },
                    conditions = conditions.takeIf { it.isNotEmpty() },
                    entities = entities.toMutableList()
                )
            }

            SpawnDefinition.SpawnConfigType.BLOCK_DROP -> {
                val blocks = data.getAsList("blocks", String::class.java) ?: listOf()
                BlockDropTrigger(
                    enabled = enabled,
                    attempts = attempts,
                    chance = chance,
                    whitelist = whitelist.takeIf { it.isNotEmpty() },
                    blacklist = blacklist.takeIf { it.isNotEmpty() },
                    conditions = conditions.takeIf { it.isNotEmpty() },
                    blocks = blocks.toMutableList()
                )
            }

            else -> SpawnDefinition(
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