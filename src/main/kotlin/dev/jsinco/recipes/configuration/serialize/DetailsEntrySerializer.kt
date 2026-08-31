package dev.jsinco.recipes.configuration.serialize

import dev.jsinco.recipes.configuration.DetailsEntry
import dev.jsinco.recipes.configuration.Visibility
import eu.okaeri.configs.schema.GenericsDeclaration
import eu.okaeri.configs.serdes.DeserializationData
import eu.okaeri.configs.serdes.ObjectSerializer
import eu.okaeri.configs.serdes.SerializationData
import net.kyori.adventure.text.Component

object DetailsEntrySerializer : ObjectSerializer<DetailsEntry> {

    override fun supports(type: Class<*>): Boolean {
        return DetailsEntry::class.java.isAssignableFrom(type)
    }

    override fun serialize(
        `object`: DetailsEntry,
        data: SerializationData,
        generics: GenericsDeclaration
    ) {
        `object`.visibility?.let { data.add("visibility", it) }
        `object`.hint?.let { data.add("hint", it) }
        `object`.effect?.let { data.add("effect", it) }
        `object`.author?.let { data.add("author", it) }
    }

    override fun deserialize(
        data: DeserializationData,
        generics: GenericsDeclaration
    ): DetailsEntry {
        return DetailsEntry(
            data.get("visibility", Visibility::class.java),
            data.getAsList("hint", Component::class.java),
            data.getAsList("effect", Component::class.java),
            data.get("author", Component::class.java)
        )
    }

}
