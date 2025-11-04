package dev.jsinco.recipes.configuration.serialize

import dev.jsinco.recipes.configuration.spawning.ConditionsDefinition
import eu.okaeri.configs.schema.GenericsDeclaration
import eu.okaeri.configs.serdes.DeserializationData
import eu.okaeri.configs.serdes.ObjectSerializer
import eu.okaeri.configs.serdes.SerializationData

object ConditionsDefinitionSerializer : ObjectSerializer<ConditionsDefinition> {
    override fun supports(type: Class<in ConditionsDefinition>): Boolean {
        return type.equals(ConditionsDefinition::class)
    }

    override fun serialize(
        `object`: ConditionsDefinition,
        data: SerializationData,
        generics: GenericsDeclaration
    ) {
        `object`.biomeCondition?.let {
            data.add("biomes", it)
        }
        `object`.worldCondition?.let {
            data.add("worlds", it)
        }
    }

    override fun deserialize(
        data: DeserializationData,
        generics: GenericsDeclaration
    ): ConditionsDefinition? {
        val biomes = data.getAsList("biomes", String::class.java)
        val worlds = data.getAsList("worlds", String::class.java)
        if(biomes == null && worlds == null) {
            return null
        }
        return ConditionsDefinition(biomes, worlds)
    }
}