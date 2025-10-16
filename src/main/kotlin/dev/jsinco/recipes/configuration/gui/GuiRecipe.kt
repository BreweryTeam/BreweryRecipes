package dev.jsinco.recipes.configuration.gui

import dev.jsinco.recipes.configuration.ConfigItem
import eu.okaeri.configs.OkaeriConfig
import org.bukkit.Material

class GuiRecipe : OkaeriConfig() {

    var enabled: Boolean = true
    var item: ConfigItem = ConfigItem.Builder()
        .material(Material.PAPER)
        .glint(true)
        .build()


    class Builder {
        private val guiRecipe = GuiRecipe()

        fun enabled(enabled: Boolean) = apply { guiRecipe.enabled = enabled }

        fun item(item: ConfigItem) = apply { guiRecipe.item = item }

        fun build() = guiRecipe
    }
}