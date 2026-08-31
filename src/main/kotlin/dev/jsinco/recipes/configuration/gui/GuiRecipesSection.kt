package dev.jsinco.recipes.configuration.gui

import dev.jsinco.recipes.configuration.ConfigItem
import dev.jsinco.recipes.configuration.LoreConfig
import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import eu.okaeri.configs.annotation.CustomKey
import org.bukkit.Material

class GuiRecipesSection : OkaeriConfig() {

    @Comment("Set a custom item to be used for displaying recipes", "Defaults to the displayed brew's material if unset")
    @CustomKey("custom-item")
    var customItem: GuiRecipe = GuiRecipe.Builder()
        .enabled(false)
        .item(ConfigItem.Builder().material(Material.PAPER).glint(true).build())
        .build()

    @Comment("Set the item to be used for displaying undiscovered recipes")
    @CustomKey("undiscovered-item")
    var undiscoveredItem: ConfigItem = ConfigItem.Builder()
        .material(Material.GLASS_BOTTLE)
        .build()

    @Comment("Lore formatting settings")
    var lore: LoreConfig = LoreConfig()
}
