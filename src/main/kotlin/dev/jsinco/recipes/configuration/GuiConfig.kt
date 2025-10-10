package dev.jsinco.recipes.configuration

import dev.jsinco.recipes.configuration.gui.GuiBorderType
import dev.jsinco.recipes.configuration.gui.GuiOverride
import dev.jsinco.recipes.gui.GuiItem
import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import org.bukkit.Material

class GuiConfig : OkaeriConfig() {

    @Comment("Define the exact slot you want an item at in the GUI")
    var overrides: List<GuiOverride> = listOf(
        GuiOverride.Builder()
            .pos(50)
            .item(
                ConfigItem.Builder().material(Material.ARROW)
                    .name("<gray>Next")
                    .build()
            ).type(GuiItem.Type.NEXT_PAGE)
            .build(),
        GuiOverride.Builder()
            .pos(48)
            .item(
                ConfigItem.Builder().material(Material.ARROW)
                    .name("<gray>Previous")
                    .build()
            ).type(GuiItem.Type.PREVIOUS_PAGE)
            .build()
    )

    @Comment("Define borders in the recipe GUI")
    var borders: Map<GuiBorderType, ConfigItemCollection> = mapOf(
        GuiBorderType.LEFT to ConfigItemCollection(
            ConfigItem.Builder().noText(true).material(Material.GREEN_STAINED_GLASS_PANE).build()
        ),
        GuiBorderType.RIGHT to ConfigItemCollection(
            ConfigItem.Builder().noText(true).material(Material.GREEN_STAINED_GLASS_PANE).build()
        ),
        GuiBorderType.UPPER to ConfigItemCollection(
            ConfigItem.Builder().noText(true).material(Material.GREEN_STAINED_GLASS_PANE).build()
        ),
        GuiBorderType.LOWER to ConfigItemCollection(
            ConfigItem.Builder().noText(true).material(Material.GREEN_STAINED_GLASS_PANE).build()
        ),
    )
}