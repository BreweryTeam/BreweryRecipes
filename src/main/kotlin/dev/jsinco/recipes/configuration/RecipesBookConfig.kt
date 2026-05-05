package dev.jsinco.recipes.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.CustomKey
import net.kyori.adventure.text.Component
import org.bukkit.Material

class RecipesBookConfig : OkaeriConfig() {

    @CustomKey("crafting-recipe")
    var craftingRecipe = CraftingDefinition()

    @CustomKey("item")
    var item = ConfigItem.Builder()
        .material(Material.WRITTEN_BOOK)
        .name(Component.translatable("breweryrecipes.item.book"))
        .build()
}