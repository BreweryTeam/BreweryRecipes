package dev.jsinco.recipes.configuration.gui

import dev.jsinco.recipes.configuration.ConfigItemSection
import eu.okaeri.configs.OkaeriConfig
import org.bukkit.Material

class GuiOverride : OkaeriConfig() {

    var type = GuiOverrideType.NO_BEHAVIOR
    var item: ConfigItemSection = ConfigItemSection.Builder()
        .material(Material.RED_STAINED_GLASS_PANE)
        .build()
    var pos: Int = 0


    enum class GuiOverrideType {
        NEXT_BUTTON,
        PREVIOUS_BUTTON,
        NO_BEHAVIOR
    }

    class Builder {
        private val guiOverride = GuiOverride()

        fun type(type: GuiOverrideType) = apply { guiOverride.type = type }

        fun pos(pos: Int) = apply { guiOverride.pos = pos }

        fun item(item: ConfigItemSection) = apply { guiOverride.item = item }

        fun build() = guiOverride
    }
}