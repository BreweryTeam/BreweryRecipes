package dev.jsinco.recipes.recipe.lore

import dev.jsinco.recipes.recipe.RecipeViewLoreWriter
import dev.jsinco.recipes.util.TranslationUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.translation.Argument

class AuthorSection(val author: String?) : LoreSection {
    override fun type() = LoreType.AUTHOR

    override fun lore(indent: Boolean): List<Component>? {
        if (author.isNullOrBlank()) return null
        val line = TranslationUtil.render(
            Component.translatable(
                "breweryrecipes.gui.recipes.lore.author",
                Argument.string("author", author)
            ).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .colorIfAbsent(NamedTextColor.GRAY)
        )
        return listOf(
            if (indent) {
                RecipeViewLoreWriter.applyAffixes(line)
            } else {
                line
            }
        )
    }
}
