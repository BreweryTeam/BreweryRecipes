package dev.jsinco.recipes.configuration.gui

import dev.jsinco.recipes.configuration.ConfigItem
import dev.jsinco.recipes.gui.GuiItem
import eu.okaeri.configs.OkaeriConfig
import org.bukkit.Material

class GuiOverride : OkaeriConfig() {

    var type = GuiItem.Type.NO_ACTION
    var item: ConfigItem = ConfigItem.Builder()
        .material(Material.RED_STAINED_GLASS_PANE)
        .build()
    var pos: String = "0"


    class Builder {
        private val guiOverride = GuiOverride()

        fun type(type:  GuiItem.Type) = apply { guiOverride.type = type }

        fun pos(pos: String) = apply { guiOverride.pos = pos }

        fun item(item: ConfigItem) = apply { guiOverride.item = item }

        fun build() = guiOverride
    }
}