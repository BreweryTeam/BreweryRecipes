package dev.jsinco.recipes.configuration.serialize

import dev.jsinco.recipes.configuration.ConfigItem
import eu.okaeri.configs.schema.GenericsDeclaration
import eu.okaeri.configs.serdes.DeserializationData
import eu.okaeri.configs.serdes.ObjectSerializer
import eu.okaeri.configs.serdes.SerializationData
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Material

object ConfigItemSerializer : ObjectSerializer<ConfigItem> {
    override fun supports(type: Class<in ConfigItem>): Boolean {
        return ConfigItem::class.java.isAssignableFrom(type)
    }

    override fun serialize(
        `object`: ConfigItem,
        data: SerializationData,
        generics: GenericsDeclaration
    ) {
        `object`.material?.let { data.add("material", it) }
        `object`.name?.let { data.add("name", it) }
        `object`.lore?.let { data.add("lore", it) }
        if (`object`.glint) {
            data.add("glint", true)
        }
        `object`.customModelData?.let { data.add("custom-model-data", it) }
        `object`.itemModel?.let { data.add("item-model", it) }
        if (`object`.noText) {
            data.add("no-text", true)
        }
    }

    override fun deserialize(
        data: DeserializationData,
        generics: GenericsDeclaration
    ): ConfigItem? {
        val material = data.get("material", Material::class.java) ?: return null
        val builder = ConfigItem.Builder()
        builder.material(material)
        data.get("name", Component::class.java)?.let { builder.name(it) }
        data.getAsList("lore", Component::class.java)?.let { builder.lore(it) }
        data.get("glint", Boolean::class.java)?.let { builder.glint(it) }
        data.get("custom-model-data", Int::class.java)?.let { builder.customModelData(it) }
        data.get("item-model", Key::class.java)?.let { builder.itemModel(it) }
        data.get("no-text", Boolean::class.java)?.let { builder.noText(it) }
        return builder.build()
    }
}