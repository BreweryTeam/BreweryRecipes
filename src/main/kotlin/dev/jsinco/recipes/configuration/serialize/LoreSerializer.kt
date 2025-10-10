package dev.jsinco.recipes.configuration.serialize

import dev.jsinco.recipes.configuration.Lore
import eu.okaeri.configs.schema.GenericsDeclaration
import eu.okaeri.configs.serdes.DeserializationData
import eu.okaeri.configs.serdes.ObjectSerializer
import eu.okaeri.configs.serdes.SerializationData
import net.kyori.adventure.text.Component

object LoreSerializer : ObjectSerializer<Lore> {
    override fun supports(type: Class<in Lore>): Boolean {
        return Lore::class.java.isAssignableFrom(type)
    }

    override fun serialize(
        `object`: Lore,
        data: SerializationData,
        generics: GenericsDeclaration
    ) {
        data.setValueCollection(`object`.components, Component::class.java)
    }

    override fun deserialize(
        data: DeserializationData,
        generics: GenericsDeclaration
    ): Lore? {
        val lore = data.getValueAsList(Component::class.java) ?: return null
        return Lore(*lore.toTypedArray())
    }
}