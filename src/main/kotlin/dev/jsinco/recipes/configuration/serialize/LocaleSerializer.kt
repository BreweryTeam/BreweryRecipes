package dev.jsinco.recipes.configuration.serialize

import eu.okaeri.configs.schema.GenericsDeclaration
import eu.okaeri.configs.serdes.DeserializationData
import eu.okaeri.configs.serdes.ObjectSerializer
import eu.okaeri.configs.serdes.SerializationData
import java.util.*

object LocaleSerializer : ObjectSerializer<Locale> {
    override fun supports(type: Class<in Locale>): Boolean {
        return type == Locale::class.java
    }

    override fun serialize(
        `object`: Locale,
        data: SerializationData,
        generics: GenericsDeclaration
    ) {
        data.setValue(`object`.toLanguageTag())
    }

    override fun deserialize(
        data: DeserializationData,
        generics: GenericsDeclaration
    ): Locale? {
        return Locale.forLanguageTag(data.getValue(String::class.java))
    }
}