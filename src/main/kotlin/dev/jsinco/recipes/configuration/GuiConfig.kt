package dev.jsinco.recipes.configuration

import dev.jsinco.recipes.configuration.gui.GuiBorderType
import dev.jsinco.recipes.configuration.gui.GuiOverride
import dev.jsinco.recipes.configuration.gui.GuiRecipesSection
import dev.jsinco.recipes.gui.GuiItem
import dev.jsinco.recipes.gui.RecipeBookMode
import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import org.bukkit.Material

class GuiConfig : OkaeriConfig() {

    @Comment("The default mode shown when opening the recipe book (FRAGMENTS or BREWED)")
    var defaultMode: RecipeBookMode = RecipeBookMode.FRAGMENTS

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

    @Comment("Override specific slots with whatever items you like")
    var overrides: List<GuiOverride> = listOf(
        GuiOverride.Builder()
            .pos("1,7,9,17,36,44,46,52")
            .item(
                ConfigItem.Builder().material(Material.SHORT_GRASS)
                    .noText(true)
                    .build()
            ).type(GuiItem.Type.NO_ACTION)
            .build(),
        GuiOverride.Builder()
            .pos("2,6,18,26,27,35,47,51")
            .item(
                ConfigItem.Builder().material(Material.FERN)
                    .noText(true)
                    .build()
            ).type(GuiItem.Type.NO_ACTION)
            .build(),
        GuiOverride.Builder()
            .pos("3,5,48,50")
            .item(
                ConfigItem.Builder().material(Material.PINK_TULIP)
                    .noText(true)
                    .build()
            ).type(GuiItem.Type.NO_ACTION)
            .build(),
        GuiOverride.Builder()
            .pos("4")
            .item(
                ConfigItem.Builder().material(Material.LILY_PAD)
                    .noText(true)
                    .build()
            ).type(GuiItem.Type.NO_ACTION)
            .build(),
        GuiOverride.Builder()
            .pos("49")
            .item(
                ConfigItem.Builder().material(Material.LANTERN)
                    .name("<gray>Fragments Mode")
                    .build()
            ).type(GuiItem.Type.SET_MODE_FRAGMENTS)
            .build(),
        GuiOverride.Builder()
            .pos("49")
            .item(
                ConfigItem.Builder().material(Material.SOUL_LANTERN)
                    .name("<gray>Synthesized Mode")
                    .build()
            ).type(GuiItem.Type.SET_MODE_BREWED)
            .build(),
        GuiOverride.Builder()
            .pos("50")
            .item(
                ConfigItem.Builder().material(Material.ARROW)
                    .name("<gray>Next")
                    .build()
            ).type(GuiItem.Type.NEXT_PAGE)
            .build(),
        GuiOverride.Builder()
            .pos("48")
            .item(
                ConfigItem.Builder().material(Material.ARROW)
                    .name("<gray>Previous")
                    .build()
            ).type(GuiItem.Type.PREVIOUS_PAGE)
            .build()
    )

    @Comment("Settings related to the recipe items")
    var recipes: GuiRecipesSection = GuiRecipesSection()
}