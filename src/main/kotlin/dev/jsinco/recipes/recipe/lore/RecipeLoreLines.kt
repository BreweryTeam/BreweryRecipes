package dev.jsinco.recipes.recipe.lore

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.recipe.BreweryRecipe
import dev.jsinco.recipes.recipe.process.Ingredient
import dev.jsinco.recipes.recipe.process.IngredientStep
import dev.jsinco.recipes.recipe.process.Step
import dev.jsinco.recipes.recipe.process.steps.AgeStep
import dev.jsinco.recipes.recipe.process.steps.CookStep
import dev.jsinco.recipes.recipe.process.steps.MixStep
import dev.jsinco.recipes.util.ItemColorUtil
import dev.jsinco.recipes.util.TranslationUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.translation.Argument
import java.util.Locale

object RecipeLoreLines {

    private val ORDINALS = listOf("①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩")
    const val DIFFICULTY_REVEAL_INDEX = 0
    private const val STEP_REVEAL_OFFSET = DIFFICULTY_REVEAL_INDEX + 1

    enum class LineType {
        DIFFICULTY,
        STEP, // ordinal included
        DETAIL // extra info belonging to the step above (like ingredient lines)
    }

    data class FlawableLine(
        val stepIndex: Int,
        val revealIndex: Int,
        val type: LineType,
        val component: Component
    )

    fun all(recipe: BreweryRecipe): List<FlawableLine> {
        return listOf(difficulty(recipe)) + steps(recipe.steps, false)
    }

    fun difficulty(recipe: BreweryRecipe): FlawableLine {
        return FlawableLine(0, DIFFICULTY_REVEAL_INDEX, LineType.DIFFICULTY, DifficultySection.buildLine(recipe))
    }

    fun steps(steps: List<Step>, isBrewNote: Boolean): List<FlawableLine> {
        val output = mutableListOf<FlawableLine>()
        steps.forEachIndexed { stepIndex, step ->
            output.add(line(stepIndex, LineType.STEP, output.size, stepLine(step, stepIndex, isBrewNote)))
            if (step is IngredientStep) {
                for ((ingredient, amount) in step.ingredients()) {
                    output.add(line(stepIndex, LineType.DETAIL, output.size, ingredientLine(ingredient, amount)))
                }
            }
            vesselLine(step)?.let { output.add(line(stepIndex, LineType.DETAIL, output.size, it)) }
        }
        return output
    }

    private fun line(stepIndex: Int, type: LineType, position: Int, component: Component) =
        FlawableLine(stepIndex, STEP_REVEAL_OFFSET + position, type, component)

    private fun stepLine(step: Step, stepIndex: Int, isBrewNote: Boolean): Component {
        val body = (if (isBrewNote) step.displayBrewNote() else step.display())
            .colorIfAbsent(NamedTextColor.GRAY)
            .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        return TranslationUtil.render(
            Component.translatable(
                "breweryrecipes.gui.recipes.lore.step.header",
                Argument.tagResolver(
                    Placeholder.unparsed("ordinal", ordinal(stepIndex)),
                    Placeholder.component("step", body)
                )
            ).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        )
    }

    private fun ordinal(stepIndex: Int): String = ORDINALS.getOrElse(stepIndex) { "${stepIndex + 1}." }

    private fun ingredientLine(ingredient: Ingredient, amount: Int): Component {
        val itemColor = BreweryRecipes.brewingIntegration
            .ingredientColor(ingredient.key)
            ?.let { TextColor.color(it.asRGB()) }
            ?: ItemColorUtil.getHex(ingredient.key)
                ?.let { TextColor.fromHexString(it) }
        val itemColorTag = itemColor
            ?.let { Tag.styling(it) }
            ?: Tag.selfClosingInserting(Component.empty())
        val brewColorTag = if (ingredient.key.startsWith("brewery:"))
            BreweryRecipes.brewingIntegration.brewIngredientColor(ingredient.key)
                ?.let { TextColor.color(it.asRGB()) }
                ?.let { Tag.styling(it) }
                ?: Tag.selfClosingInserting(Component.empty())
        else Tag.selfClosingInserting(Component.empty())
        return Component.translatable(
            "breweryrecipes.gui.recipes.lore.step.ingredient",
            Argument.tagResolver(
                Formatter.number("count", amount),
                Placeholder.component("name", ingredient.displayName),
                TagResolver.resolver("itemcolor", itemColorTag),
                TagResolver.resolver("brewcolor", brewColorTag)
            )
        ).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
    }

    private fun vesselLine(step: Step): Component? = when (step) {
        is CookStep -> step.cauldronType?.let { cauldronLine(it.name) }
        is MixStep -> step.cauldronType?.let { cauldronLine(it.name) }
        is AgeStep -> vesselTypeLine(
            "breweryrecipes.gui.recipes.lore.step.barrel",
            "breweryrecipes.gui.recipes.lore.step.barrel.type.${step.barrelType.name.lowercase(Locale.ROOT)}",
            "barrel_type"
        )
        else -> null // distill step doesn't have detail lines
    }

    private fun cauldronLine(cauldronType: String): Component = vesselTypeLine(
        "breweryrecipes.gui.recipes.lore.step.cauldron",
        "breweryrecipes.gui.recipes.lore.step.cauldron.type.${cauldronType.lowercase(Locale.ROOT)}",
        "cauldron_type"
    )

    private fun vesselTypeLine(lineKey: String, typeKey: String, typePlaceholder: String): Component {
        return Component.translatable(
            lineKey,
            Argument.tagResolver(Placeholder.component(typePlaceholder, Component.translatable(typeKey)))
        ).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
    }
}
