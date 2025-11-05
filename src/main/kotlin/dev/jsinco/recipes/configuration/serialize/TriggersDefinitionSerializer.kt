package dev.jsinco.recipes.configuration.serialize

import dev.jsinco.recipes.configuration.spawning.triggers.*
import dev.jsinco.recipes.util.Logger
import eu.okaeri.configs.schema.GenericsDeclaration
import eu.okaeri.configs.serdes.DeserializationData
import eu.okaeri.configs.serdes.ObjectSerializer
import eu.okaeri.configs.serdes.SerializationData
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
        `object`.inventoryFillTrigger?.let { data.add("inventory", it.inventoryTypes) }
        `object`.blockDropTrigger?.let { data.add("block", it.blocks) }
        `object`.lootSpawnTrigger?.let { data.add("loot", it.lootTables.map(LootTable::key)) }
        `object`.mobDropTrigger?.let { data.add("entities", it.entities) }
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
        return if (output.asList().isEmpty()) {
            null
        } else {
            output
        }
    }
}