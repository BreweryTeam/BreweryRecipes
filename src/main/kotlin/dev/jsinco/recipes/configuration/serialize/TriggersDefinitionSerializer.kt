package dev.jsinco.recipes.configuration.serialize

import dev.jsinco.recipes.configuration.spawning.triggers.*
import eu.okaeri.configs.schema.GenericsDeclaration
import eu.okaeri.configs.serdes.DeserializationData
import eu.okaeri.configs.serdes.ObjectSerializer
import eu.okaeri.configs.serdes.SerializationData
import net.kyori.adventure.key.Key
import org.bukkit.block.BlockType
import org.bukkit.entity.EntityType
import org.bukkit.event.inventory.InventoryType
import org.bukkit.loot.LootTable

object TriggersDefinitionSerializer : ObjectSerializer<TriggersDefinition> {
    override fun supports(type: Class<in TriggersDefinition>): Boolean {
        return TriggersDefinition::class.java.isAssignableFrom(type)
    }

    override fun serialize(
        `object`: TriggersDefinition,
        data: SerializationData,
        generics: GenericsDeclaration
    ) {
        `object`.premadeTrigger?.let { data.add("premade", it) }
        `object`.inventoryFillTrigger?.let {
            data.addCollection(
                "inventory",
                it.inventoryTypes.toList(),
                InventoryType::class.java
            )
        }
        `object`.blockDropTrigger?.let { data.addCollection("block", it.blocks.toList(), BlockType::class.java) }
        `object`.lootSpawnTrigger?.let {
            data.addCollection(
                "loot",
                it.lootTables.map(LootTable::key),
                Key::class.java
            )
        }
        `object`.mobDropTrigger?.let { data.addCollection("entities", it.entities.toList(), EntityType::class.java) }
    }

    override fun deserialize(
        data: DeserializationData,
        generics: GenericsDeclaration
    ): TriggersDefinition? {
        val premadeTrigger = data.getAsList("premade", PremadeTrigger::class.java)
        val inventoryFillTrigger = data.getAsList("inventory", InventoryType::class.java)
        val blockDropTrigger = data.getAsList("block", BlockType::class.java)
        val lootSpawnTrigger = data.getAsList("loot", String::class.java)
        val mobDropTrigger = data.getAsList("entities", EntityType::class.java)
        val output = TriggersDefinition(
            premadeTrigger = premadeTrigger,
            inventoryFillTrigger = inventoryFillTrigger?.let { InventoryFillTrigger(*it.toTypedArray()) },
            blockDropTrigger = blockDropTrigger?.let { BlockDropTrigger(*it.toTypedArray()) },
            lootSpawnTrigger = lootSpawnTrigger?.let { LootSpawnTrigger.fromStrings(*it.toTypedArray()) },
            mobDropTrigger = mobDropTrigger?.let { MobDropTrigger(*it.toTypedArray()) }
        )
        return if (output.asList().isEmpty() && output.premadeTrigger?.isEmpty() ?: false) {
            null
        } else {
            output
        }
    }
}