package dev.jsinco.recipes.configuration.serialize

import dev.jsinco.recipes.util.Logger
import eu.okaeri.configs.schema.GenericsDeclaration
import eu.okaeri.configs.serdes.DeserializationData
import eu.okaeri.configs.serdes.ObjectSerializer
import eu.okaeri.configs.serdes.SerializationData
import net.kyori.adventure.key.Key

object KeySerializer : ObjectSerializer<Key> {
    override fun supports(type: Class<in Key>): Boolean {
        return Key::class.java.isAssignableFrom(type)
    }

    override fun serialize(
        `object`: Key,
        data: SerializationData,
        generics: GenericsDeclaration
    ) {
        data.setValue(`object`.asMinimalString())
    }

    override fun deserialize(
        data: DeserializationData,
        generics: GenericsDeclaration
    ): Key? {
        val value = data.getValue(String::class.java)
        if (!Key.parseable(value)) {
            Logger.logErr("Could not parse key '$value' in ${data.context.field?.name ?: "unknown"}")
            return null
        }
        return Key.key(value)
    }
}