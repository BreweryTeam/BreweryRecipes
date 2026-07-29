package dev.jsinco.recipes.recipe.lore

import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.RecipeViewLoreWriter
import dev.jsinco.recipes.util.TranslationUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.translation.Argument

class DifficultySection(val recipe: BreweryRecipe) : LoreSection {
    override fun type() = LoreType.DIFFICULTY

    override fun lore(indent: Boolean): List<Component> {
        val difficulty = recipe.difficulty
        val line = TranslationUtil.render(
            Component.translatable(
                "breweryrecipes.gui.recipes.lore.difficulty",
                Argument.tagResolver(
                    TagResolver.resolver("difficultycolor", Tag.styling(difficultyColor(difficulty))),
                    Placeholder.unparsed("difficulty", formatDifficulty(difficulty))
                )
            ).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        )
        return listOf(
            if (indent) {
                RecipeViewLoreWriter.applyAffixes(line)
            } else {
                line
            }
        )
    }

    private fun formatDifficulty(difficulty: Double): String =
        "%.2f".format(difficulty).trimEnd('0').trimEnd('.')

    private fun difficultyColor(difficulty: Double): TextColor {
        val green = TextColor.color(0x55FF55)
        val yellow = TextColor.color(0xFFFF55)
        val red = TextColor.color(0xFF5555)
        val darkRed = TextColor.color(0xCC2222)

        return if (difficulty <= 10.0) {
            val normalized = (difficulty / 10.0).coerceIn(0.0, 1.0).toFloat()
            when {
                normalized < 0.5f -> TextColor.lerp(normalized / 0.5f, green, yellow)
                else -> TextColor.lerp((normalized - 0.5f) / 0.5f, yellow, red)
            }
        } else {
            val normalized = ((difficulty - 10.0) / 10.0).coerceIn(0.0, 1.0).toFloat()
            TextColor.lerp(normalized, red, darkRed)
        }
    }
}
