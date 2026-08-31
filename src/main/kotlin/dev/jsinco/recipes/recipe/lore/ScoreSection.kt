package dev.jsinco.recipes.recipe.lore

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.RecipeViewLoreWriter
import dev.jsinco.recipes.util.TranslationUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.translation.Argument

class ScoreSection(val recipe: BreweryRecipe) : LoreSection {
    override fun type() = LoreType.SCORE

    override fun lore(indent: Boolean): List<Component>? {
        val scoreComponent = BreweryRecipes.brewingIntegration.scoreDisplayName(recipe) ?: return null
        val line = TranslationUtil.render(
            Component.translatable(
                "breweryrecipes.gui.recipes.lore.quality",
                Argument.component("qualitystars", scoreComponent)
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
