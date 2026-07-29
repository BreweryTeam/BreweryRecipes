package dev.jsinco.recipes.recipe.lore

import dev.jsinco.recipes.recipe.RecipeViewLoreWriter
import dev.jsinco.recipes.util.TranslationUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.translation.Argument

class HintSection(val hint: List<String>) : LoreSection {
    override fun type() = LoreType.HINT

    override fun lore(indent: Boolean): List<Component>? {
        if (hint.isEmpty()) return null
        return hint.map { hintLine ->
            val line = TranslationUtil.render(
                Component.translatable(
                    "breweryrecipes.gui.recipes.lore.hint",
                    Argument.string("hint", hintLine)
                ).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    .colorIfAbsent(NamedTextColor.GRAY)
            )
            if (indent) {
                RecipeViewLoreWriter.applyAffixes(line)
            } else {
                line
            }
        }
    }
}
