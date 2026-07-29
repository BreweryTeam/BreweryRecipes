package dev.jsinco.recipes.recipe.lore

import dev.jsinco.recipes.BreweryRecipes
import dev.jsinco.recipes.recipe.RecipeView
import dev.jsinco.recipes.recipe.RecipeViewLoreWriter
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
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

class StepsSection(
    val steps: List<Step>,
    val recipeView: RecipeView,
    val isBrewNote: Boolean
) : LoreSection {
    private val ordinals = listOf("①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩")

    override fun type() = LoreType.STEPS

    override fun lore(indent: Boolean): List<Component> {
        val loreConfig = BreweryRecipes.guiConfig.recipes.lore

        val result = mutableListOf<Component>()
        steps.forEachIndexed { index, step ->
            val stepComponent = RecipeViewLoreWriter.renderStep(step, index, recipeView.flaws, recipeView.invertedReveals, isBrewNote)
                .colorIfAbsent(NamedTextColor.GRAY)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            val rendered = TranslationUtil.render(stepComponent)
            val ordinal = ordinals.getOrElse(index) { "${index + 1}." }

            val ordinalLine = TranslationUtil.render(
                Component.translatable(
                    "breweryrecipes.gui.recipes.lore.step.header",
                    Argument.tagResolver(
                        Placeholder.unparsed("ordinal", ordinal),
                        Placeholder.component("step", rendered)
                    )
                ).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            )
            result.add(ordinalLine)

            if (step is IngredientStep) {
                for ((ingredient, amount) in step.ingredients()) {
                    val itemColorTag = ItemColorUtil.getHex(ingredient.key)
                        ?.let { TextColor.fromHexString(it) }
                        ?.let { Tag.styling(it) }
                        ?: Tag.selfClosingInserting(Component.empty())
                    val brewColorTag = if (ingredient.key.startsWith("brewery:"))
                        BreweryRecipes.brewingIntegration.brewIngredientColor(ingredient.key)
                            ?.let { TextColor.color(it.asRGB()) }
                            ?.let { Tag.styling(it) }
                            ?: Tag.selfClosingInserting(Component.empty())
                    else Tag.selfClosingInserting(Component.empty())
                    val ingredientComp = Component.translatable(
                        "breweryrecipes.gui.recipes.lore.step.ingredient",
                        Argument.tagResolver(
                            Formatter.number("count", amount),
                            Placeholder.component("name", ingredient.displayName),
                            TagResolver.resolver("itemcolor", itemColorTag),
                            TagResolver.resolver("brewcolor", brewColorTag)
                        )
                    ).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    val line = TranslationUtil.render(
                        RecipeViewLoreWriter.applyFlaws(ingredientComp, index, recipeView.flaws, recipeView.invertedReveals)
                    )
                    result.add(if (indent) {
                        RecipeViewLoreWriter.applyAffixes(line)
                    } else {
                        line
                    })
                }
            }

            val line = when (step) {
                is CookStep -> step.cauldronType?.let {
                    TranslationUtil.render(
                        RecipeViewLoreWriter.applyFlaws(buildTypeLine(
                            "breweryrecipes.gui.recipes.lore.step.cauldron",
                            "breweryrecipes.gui.recipes.lore.step.cauldron.type.${it.name.lowercase(Locale.ROOT)}",
                            "cauldron_type"
                        ), index, recipeView.flaws, recipeView.invertedReveals)
                    )
                }
                is MixStep -> step.cauldronType?.let {
                    TranslationUtil.render(
                        RecipeViewLoreWriter.applyFlaws(buildTypeLine(
                            "breweryrecipes.gui.recipes.lore.step.cauldron",
                            "breweryrecipes.gui.recipes.lore.step.cauldron.type.${it.name.lowercase(Locale.ROOT)}",
                            "cauldron_type"
                        ), index, recipeView.flaws, recipeView.invertedReveals))
                }
                is AgeStep -> {
                    TranslationUtil.render(
                        RecipeViewLoreWriter.applyFlaws(buildTypeLine(
                            "breweryrecipes.gui.recipes.lore.step.barrel",
                            "breweryrecipes.gui.recipes.lore.step.barrel.type.${step.barrelType.name.lowercase(Locale.ROOT)}",
                            "barrel_type"
                        ), index, recipeView.flaws, recipeView.invertedReveals)
                    )
                }
                else -> null
            }
            line?.let {
                result.add(if (indent) {
                    RecipeViewLoreWriter.applyAffixes(it)
                } else {
                    it
                })
            }

            if (loreConfig.emptyLineBetweenSteps && index < steps.size - 1) {
                result.add(Component.empty())
            }
        }

        return result
    }

    private fun buildTypeLine(lineKey: String, typeKey: String, typePlaceholder: String): Component {
        return Component.translatable(
            lineKey,
            Argument.tagResolver(Placeholder.component(typePlaceholder, Component.translatable(typeKey)))
        ).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
    }
}
