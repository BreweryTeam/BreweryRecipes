package dev.jsinco.recipes.configuration.gui

import dev.jsinco.recipes.configuration.ConfigItem
import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import eu.okaeri.configs.annotation.CustomKey
import org.bukkit.Material

class GuiGroupsSection : OkaeriConfig() {

    @Comment("The item used for the \"All Recipes\" recipe group")
    @CustomKey("all-item")
    var allItem: GuiRecipe = GuiRecipe.Builder()
        .item(ConfigItem.Builder().name("<gold>All Recipes").material(Material.BREWING_STAND).glint(true).build())
        .build()

    @Comment("The default item to be used if a group is not assigned an item")
    @CustomKey("default-item")
    var defaultItem: ConfigItem = ConfigItem.Builder()
        .material(Material.WHEAT_SEEDS)
        .build()

    @Comment("Groups hidden from the GUI")
    @CustomKey("hidden-groups")
    var hiddenGroups: List<String> = listOf()

    @Comment("Set the item each group uses in the groups menu")
    @CustomKey("group-items")
    var groupItems: Map<String, ConfigItem> = mapOf(
        "beers" to ConfigItem.Builder()
            .material(Material.WHEAT)
            .build()
    )

}
