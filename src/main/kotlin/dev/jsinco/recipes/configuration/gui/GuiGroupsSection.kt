package dev.jsinco.recipes.configuration.gui

import dev.jsinco.recipes.configuration.ConfigItem
import dev.jsinco.recipes.configuration.GroupPosition
import dev.jsinco.recipes.configuration.SortOrder
import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import eu.okaeri.configs.annotation.CustomKey
import org.bukkit.Material

class GuiGroupsSection : OkaeriConfig() {

    @Comment(
        "How groups are ordered in the recipe book:",
        "ALPHABETICAL_IDENTIFIER: sorted alphabetically by group identifier",
        "ALPHABETICAL_NAME: sorted by the group's display name (ignoring color codes/tags)",
        "AS_PROVIDED: keep the order in which the providing brewing plugin relays its groups"
    )
    @CustomKey("group-sort-order")
    var groupSortOrder: SortOrder = SortOrder.AS_PROVIDED

    @Comment("The item used for the \"All Recipes\" recipe group")
    @CustomKey("all-recipes-item")
    var allRecipesItem: GuiRecipe = GuiRecipe.Builder()
        .item(ConfigItem.Builder().name("<gold>All Recipes").material(Material.BREWING_STAND).glint(true).build())
        .build()

    @Comment(
        "Where in the group list the \"All Recipes\" recipe group should appear",
        "START: at the start of the list",
        "END: at the end of the list"
    )
    @CustomKey("all-recipes-position")
    var allRecipesPosition: GroupPosition = GroupPosition.START

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
