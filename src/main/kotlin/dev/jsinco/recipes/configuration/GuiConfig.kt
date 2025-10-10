package dev.jsinco.recipes.configuration

import dev.jsinco.recipes.configuration.gui.GuiBorderType
import dev.jsinco.recipes.configuration.gui.GuiOverride
import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import org.bukkit.Material

class GuiConfig : OkaeriConfig() {

    @Comment("Define the exact slot you want an item at in the gui")
    var overrides = listOf(
        GuiOverride.Builder()
            .pos(51)
            .item(ConfigItemSection.Builder().material(Material.ARROW)
                .name("<gray>Next")
                .build()
            ).type(GuiOverride.GuiOverrideType.NEXT_BUTTON)
            .build(),
        GuiOverride.Builder()
            .pos(49)
            .item(ConfigItemSection.Builder().material(Material.ARROW)
                .name("<gray>Previous")
                .build()
            ).type(GuiOverride.GuiOverrideType.PREVIOUS_BUTTON)
            .build()
    )

    @Comment("Define borders in the recipe gui")
    var borders = mapOf(
        GuiBorderType.LEFT to listOf(
            ConfigItemSection.Builder().noText(true).material(Material.GREEN_STAINED_GLASS_PANE).build()
        ),
        GuiBorderType.RIGHT to listOf(
            ConfigItemSection.Builder().noText(true).material(Material.GREEN_STAINED_GLASS_PANE).build()
        ),
        GuiBorderType.UPPER to listOf(
            ConfigItemSection.Builder().noText(true).material(Material.GREEN_STAINED_GLASS_PANE).build()
        ),
        GuiBorderType.LOWER to listOf(
            ConfigItemSection.Builder().noText(true).material(Material.GREEN_STAINED_GLASS_PANE).build()
        ),
    )
}