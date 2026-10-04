package dev.jsinco.recipes.gui

import dev.jsinco.recipes.recipe.BreweryRecipeGroup
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument
import org.bukkit.entity.Player
import java.util.Locale

enum class RecipeBookMode {
    FRAGMENTS,
    BREWED;

    fun identifier() = name.lowercase(Locale.ROOT)

    fun guiName(admin: Boolean, group: BreweryRecipeGroup?): Component {
        val baseKey = if (admin) {
            "breweryrecipes.gui.name.admin.${identifier()}"
        } else {
            "breweryrecipes.gui.name.${identifier()}"
        }
        return if (group != null) {
            Component.translatable("${baseKey}_group", Argument.component("group", group.displayName))
        } else {
            Component.translatable(baseKey)
        }
    }

    fun hasOverridePermission(player: Player) = player.hasPermission("breweryrecipes.override.view.${identifier()}")

    fun next(): RecipeBookMode = entries[(ordinal + 1) % entries.size]
}
