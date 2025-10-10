package dev.jsinco.recipes.configuration

import com.dre.brewery.depend.okaeri.configs.annotation.CustomKey
import eu.okaeri.configs.OkaeriConfig
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.NamespacedKey


class ConfigItemSection : OkaeriConfig() {

    var material: Material? = null

    @CustomKey("display-name")
    var name: Component? = null
    var lore: List<Component>? = null
    var glint: Boolean? = null

    @CustomKey("custom-model-data")
    var customModelData: Int? = null
    @CustomKey("item-model")
    var itemModel: Key? = null
    @CustomKey("no-text")
    var noText = false


    class Builder {
        private val configItemSection = ConfigItemSection()

        fun material(material: Material) = apply { configItemSection.material = material }
        fun name(name: String) = apply { configItemSection.name = MiniMessage.miniMessage().deserialize(name) }
        fun lore(vararg lore: String) = apply { configItemSection.lore = lore
            .map { MiniMessage.miniMessage().deserialize(it) }
            .toList()
        }
        fun glint(glint: Boolean) = apply { configItemSection.glint = glint }
        fun customModelData(customModelData: Int) = apply { configItemSection.customModelData = customModelData }
        fun itemModel(itemModel: String) = apply { configItemSection.itemModel = NamespacedKey.fromString(itemModel) }
        fun noText(noText: Boolean) = apply { configItemSection.noText = noText }

        fun build() = configItemSection
    }
}