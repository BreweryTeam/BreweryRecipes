package dev.jsinco.recipes.recipe.lore

import dev.jsinco.recipes.recipe.RecipeViewLoreWriter
import dev.jsinco.recipes.util.TranslationUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.translation.Argument

class EffectSection(val effect: List<String>) : LoreSection {
    override fun type() = LoreType.EFFECT

    override fun lore(indent: Boolean): List<Component>? {
        if (effect.isEmpty()) return null
        return effect.map { effectLine ->
            val line = TranslationUtil.render(
                Component.translatable(
                    "breweryrecipes.gui.recipes.lore.effect",
                    Argument.string("effect", effectLine)
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
