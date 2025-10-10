package dev.jsinco.recipes.configuration.serialize

import eu.okaeri.configs.schema.GenericsDeclaration
import eu.okaeri.configs.serdes.DeserializationData
import eu.okaeri.configs.serdes.ObjectSerializer
import eu.okaeri.configs.serdes.SerializationData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

object ComponentSerializer : ObjectSerializer<Component> {
    override fun supports(type: Class<in Component>): Boolean {
        return type.isAssignableFrom(Component::class.java)
    }

    override fun serialize(
        `object`: Component,
        data: SerializationData,
        generics: GenericsDeclaration
    ) {
        data.setValue(MiniMessage.miniMessage().serialize(`object`))
    }

    override fun deserialize(
        data: DeserializationData,
        generics: GenericsDeclaration
    ): Component? {
        val value = data.getValue(String::class.java) ?: return null
        return MiniMessage.miniMessage().deserialize(value)
    }
}