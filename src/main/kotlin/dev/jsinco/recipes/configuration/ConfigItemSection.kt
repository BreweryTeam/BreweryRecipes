package dev.jsinco.recipes.configuration

import com.dre.brewery.depend.okaeri.configs.annotation.CustomKey
import eu.okaeri.configs.OkaeriConfig
import org.bukkit.Material


class ConfigItemSection : OkaeriConfig() {

    var material: Material? = null
    var slots: List<Int>? = null

    @CustomKey("display_name")
    var name: String? = null
    var lore: List<String>? = null
    var glint: Boolean? = null

    @CustomKey("custom_model_data")
    var customModelData: Int? = null


    class Builder {
        private val configItemSection = ConfigItemSection()

        fun material(material: Material) = apply { configItemSection.material = material }
        fun slots(vararg slots: Int) = apply { configItemSection.slots = slots.toList() }
        fun name(name: String) = apply { configItemSection.name = name }
        fun lore(vararg lore: String) = apply { configItemSection.lore = lore.toList() }
        fun glint(glint: Boolean) = apply { configItemSection.glint = glint }
        fun customModelData(customModelData: Int) = apply { configItemSection.customModelData = customModelData }


        fun build() = configItemSection
    }
}