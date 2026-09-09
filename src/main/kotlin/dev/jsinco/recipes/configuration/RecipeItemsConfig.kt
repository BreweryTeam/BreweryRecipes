package dev.jsinco.recipes.configuration

import dev.jsinco.recipes.configuration.gui.GuiRecipe
import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import eu.okaeri.configs.annotation.CustomKey
import org.bukkit.Material

class RecipeItemsConfig : OkaeriConfig() {

    @Comment("Recipe items are physical copies of a single recipe as you would see it in the recipe book")
    var enabled: Boolean = true

    @Comment("Override a recipe item's material, which would otherwise default to the brew's material")
    @CustomKey("item-override")
    var itemOverride: GuiRecipe = GuiRecipe.Builder()
        .enabled(true)
        .item(ConfigItem.Builder().material(Material.PAPER).glint(true).build())
        .build()

    @Comment("Allow recipe items to be redeemed into a player's recipe book by right-clicking them?")
    @CustomKey("redeem-on-right-click")
    var redeemOnRightClick: Boolean = false

    @Comment("Allow recipe items to be merged into the recipe book by dropping them onto it in an inventory?")
    @CustomKey("merge-onto-recipe-book")
    var mergeOntoRecipeBook: Boolean = false

    @Comment("Allow recipe items to be merged into the recipe book by putting both into a crafting grid?")
    @CustomKey("merge-in-crafting-grid")
    var mergeInCraftingGrid: Boolean = true

    @Comment("Allow recipe items to be combined into a single (more complete) recipe item in a crafting grid?")
    @CustomKey("combine-in-crafting-grid")
    var combineInCraftingGrid: Boolean = true
}
