package dev.jsinco.recipes.configuration.serialize

import dev.jsinco.recipes.configuration.ConfigItem
import dev.jsinco.recipes.configuration.ConfigItemCollection
import eu.okaeri.configs.schema.GenericsDeclaration
import eu.okaeri.configs.serdes.DeserializationData
import eu.okaeri.configs.serdes.ObjectSerializer
import eu.okaeri.configs.serdes.SerializationData

object ConfigItemCollectionSerializer : ObjectSerializer<ConfigItemCollection> {
    override fun supports(type: Class<in ConfigItemCollection>): Boolean {
        return ConfigItemCollection::class.java.isAssignableFrom(type)
    }

    override fun serialize(
        `object`: ConfigItemCollection,
        data: SerializationData,
        generics: GenericsDeclaration
    ) {
        return data.setValueCollection(`object`.content, ConfigItem::class.java)
    }

    override fun deserialize(
        data: DeserializationData,
        generics: GenericsDeclaration
    ): ConfigItemCollection? {
        val content = data.getValueAsList(ConfigItem::class.java) ?: return null
        return ConfigItemCollection(*content.toTypedArray())
    }
}