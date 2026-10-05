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
    var hiddenGroups: List<String> = listOf(
        "alcoholic",
        "beers"
    )

    @Comment("Set the item each group uses in the groups menu")
    @CustomKey("group-items")
    var groupItems: Map<String, ConfigItem> = mapOf(
        "ale" to ConfigItem.Builder()
            .material(Material.POTION)
            .potionColor("#ff7518")
            .build(),
        "beer" to ConfigItem.Builder()
            .material(Material.POTION)
            .potionColor("#ffd333")
            .build(),
        "stout" to ConfigItem.Builder()
            .material(Material.POTION)
            .potionColor("#8b4513")
            .build(),
        "absinthe" to ConfigItem.Builder()
            .material(Material.POTION)
            .potionColor("GREEN")
            .build(),
        "aperitifs" to ConfigItem.Builder()
            .material(Material.POTION)
            .potionColor("#ff5e12")
            .build(),
        "brandy" to ConfigItem.Builder()
            .material(Material.POTION)
            .potionColor("#cc7241")
            .build(),
        "gin" to ConfigItem.Builder()
            .material(Material.POTION)
            .potionColor("#99ddff")
            .build(),
        "liquor" to ConfigItem.Builder()
            .material(Material.POTION)
            .potionColor("#ffd700")
            .build(),
        "rum" to ConfigItem.Builder()
            .material(Material.POTION)
            .potionColor("#ffbf54")
            .build(),
        "vodka" to ConfigItem.Builder()
            .material(Material.POTION)
            .potionColor("#f5f5dc")
            .build(),
        "whiskey" to ConfigItem.Builder()
            .material(Material.POTION)
            .potionColor("#d2b48c")
            .build(),
        "cider" to ConfigItem.Builder()
            .material(Material.POTION)
            .potionColor("#f86820")
            .build(),
        "cocktail" to ConfigItem.Builder()
            .material(Material.POTION)
            .potionColor("#f5aa4e")
            .build(),
        "mead" to ConfigItem.Builder()
            .material(Material.HONEY_BOTTLE)
            .build(),
        "wine" to ConfigItem.Builder()
            .material(Material.POTION)
            .potionColor("#c70039")
            .build(),
        "coffee" to ConfigItem.Builder()
            .material(Material.POTION)
            .potionColor("BLACK")
            .build(),
        "juice" to ConfigItem.Builder()
            .material(Material.POTION)
            .potionColor("#af2a2d")
            .build(),
        "non_alcoholic" to ConfigItem.Builder()
            .material(Material.POTION)
            .potionColor("#32cd32")
            .build(),
        "tea" to ConfigItem.Builder()
            .material(Material.POTION)
            .potionColor("#009b14")
            .build()
    )

}
