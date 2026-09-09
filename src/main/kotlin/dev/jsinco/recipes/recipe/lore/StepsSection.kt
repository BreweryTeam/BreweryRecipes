package dev.jsinco.recipes.recipe.lore

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.recipe.RecipeView
import dev.jsinco.recipes.recipe.RecipeViewLoreWriter
import dev.jsinco.recipes.recipe.process.Step
import dev.jsinco.recipes.util.TranslationUtil
import net.kyori.adventure.text.Component

class StepsSection(
    val steps: List<Step>,
    val recipeView: RecipeView,
    val isBrewNote: Boolean
) : LoreSection {
    override fun type() = LoreType.STEPS

    override fun lore(indent: Boolean): List<Component> {
        val loreConfig = BreweryRecipes.guiConfig.recipes.lore
        val lines = RecipeLoreLines.steps(steps, isBrewNote)

        val result = mutableListOf<Component>()
        lines.forEachIndexed { index, flawableLine ->
            val line = TranslationUtil.render(
                RecipeViewLoreWriter.applyFlaws(
                    flawableLine.component,
                    flawableLine.stepIndex,
                    recipeView.flaws,
                    recipeView.invertedReveals,
                    flawableLine.revealIndex
                )
            )
            result.add(if (indent && flawableLine.type == RecipeLoreLines.LineType.DETAIL) RecipeViewLoreWriter.applyAffixes(line) else line)
            val nextLine = lines.getOrNull(index + 1) ?: return@forEachIndexed
            if (loreConfig.emptyLineBetweenSteps && nextLine.stepIndex != flawableLine.stepIndex) {
                result.add(Component.empty())
            }
        }

        return result
    }
}
