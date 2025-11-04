package dev.jsinco.recipes.configuration.serialize

import dev.jsinco.recipes.configuration.spawning.triggers.TriggersDefinition
import eu.okaeri.configs.schema.GenericsDeclaration
import eu.okaeri.configs.serdes.DeserializationData
import eu.okaeri.configs.serdes.ObjectSerializer
import eu.okaeri.configs.serdes.SerializationData

object TriggersDefinitionSerializer : ObjectSerializer<TriggersDefinition> {
    override fun supports(type: Class<in TriggersDefinition>): Boolean {
        return type.equals(TriggersDefinition::class)
    }

    override fun serialize(
        `object`: TriggersDefinition,
        data: SerializationData,
        generics: GenericsDeclaration
    ) {

    }

    override fun deserialize(
        data: DeserializationData,
        generics: GenericsDeclaration
    ): TriggersDefinition? {
        TODO("Not yet implemented")
    }
}