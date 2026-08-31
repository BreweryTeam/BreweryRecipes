package dev.jsinco.recipes.gui

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.Locale

enum class RecipeBookMode {
    FRAGMENTS,
    BREWED;

    fun identifier() = name.lowercase(Locale.ROOT)

    fun guiName(admin: Boolean): Component {
        return if (admin) {
            Component.translatable("breweryrecipes.gui.name.admin.${identifier()}")
        } else {
            Component.translatable("breweryrecipes.gui.name.${identifier()}")
        }
    }

    fun hasOverridePermission(player: Player) = player.hasPermission("breweryrecipes.override.view.${identifier()}")

    fun next(): RecipeBookMode = entries[(ordinal + 1) % entries.size]
}
