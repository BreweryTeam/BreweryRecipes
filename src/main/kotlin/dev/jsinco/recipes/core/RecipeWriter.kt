package dev.jsinco.recipes.core

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.core.flaws.*
import dev.jsinco.recipes.core.process.Ingredient
import dev.jsinco.recipes.core.process.Step
import dev.jsinco.recipes.core.process.steps.AgeStep
import dev.jsinco.recipes.core.process.steps.CookStep
import dev.jsinco.recipes.core.process.steps.DistillStep
import dev.jsinco.recipes.core.process.steps.MixStep
import dev.jsinco.recipes.util.TranslationUtil
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.translation.Argument
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*

object RecipeWriter {
    // TODO: Read this from TBP and BreweryX
    const val DEFAULT_COOKING_MINUTE = 20 * 60
    const val DEFAULT_AGING_YEAR = DEFAULT_COOKING_MINUTE * 20

    fun writeToItem(recipeView: RecipeView): ItemStack? {
        val recipe = Recipes.recipes()[recipeView.recipeIdentifier] ?: return null
        val item = ItemStack(Material.PAPER) // TODO better items here
        item.setData(
            DataComponentTypes.LORE, ItemLore.lore(
                recipe.steps
                    .asSequence()
                    .mapIndexed { index, value -> renderStep(value, index, recipeView.flaws) }
                    .map { component ->
                        component.colorIfAbsent(NamedTextColor.GRAY)
                            .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    }.map { GlobalTranslator.render(it, Locale.ENGLISH) }
                    .toList()
            )
        )
        return item
    }

    private fun buildBaseStep(step: Step): Component {
        val raw: Component = when (step) {
            is DistillStep -> Component.translatable(
                "recipes.display.recipe.step.distill",
                Argument.tagResolver(Formatter.number("distill_runs", step.count))
            )

            is AgeStep -> Component.translatable(
                "recipes.display.recipe.step.age",
                Argument.tagResolver(
                    Formatter.number("aging_years", step.agingTicks / DEFAULT_AGING_YEAR),
                    Placeholder.component(
                        "barrel_type",
                        Component.translatable("recipes.barrel.type." + step.barrelType.name.lowercase(Locale.ROOT))
                    )
                )
            )

            is MixStep -> Component.translatable(
                "recipes.display.recipe.step.mix",
                Argument.tagResolver(
                    Placeholder.component("ingredients", compileIngredients(step.ingredients)),
                    Formatter.number("mixing_time", step.mixingTicks / DEFAULT_COOKING_MINUTE)
                )
            )

            is CookStep -> Component.translatable(
                "recipes.display.recipe.step.cook",
                Argument.tagResolver(
                    Placeholder.component("ingredients", compileIngredients(step.ingredients)),
                    Formatter.number("cooking_time", step.cookingTicks / DEFAULT_COOKING_MINUTE),
                    Placeholder.component(
                        "cauldron_type",
                        Component.translatable("recipes.cauldron.type." + step.cauldronType.name.lowercase(Locale.ROOT))
                    )
                )
            )

            else -> Component.text("Unknown component")
        }
        return TranslationUtil.render(raw)
    }

    private fun renderStep(step: Step, stepIndex: Int, flaws: List<FlawBundle>): Component {
        val rawBase = buildBaseStep(step)
        val base = resolveTranslatablesForMutation(rawBase)
        val textModifications = compileTextModifications(base, stepIndex, flaws)
        var output = base
        var offsets = mapOf<Int, Int>()
        for (entry in textModifications) {
            output =
                FlawTextModificationWriter.process(output, entry.value, entry.key.type, entry.key.config.seed, offsets)
            offsets = entry.value.offsets(offsets)
        }
        return output
    }

    private fun compileTextModifications(
        step: Component,
        stepIndex: Int,
        flaws: List<FlawBundle>
    ): Map<Flaw, FlawTextModifications> {
        val allTextModifications = mutableMapOf<Flaw, FlawTextModifications>()
        if (flaws.isEmpty()) {
            return allTextModifications
        }
        var allFlawPositions: MutableSet<Int>? = null
        for (bundle in flaws) {
            val flawPositions = mutableSetOf<Int>()
            for (flaw in bundle.flaws) {
                if (flawApplies(stepIndex, flaw)) {
                    val textModifications = flaw.type.findFlawModifications(step, flaw.config) {
                        !flawPositions.contains(it)
                    }.withEmptyReplacements {
                        allFlawPositions?.contains(it) ?: false
                    }
                    flawPositions.addAll(
                        textModifications.modifiedPoints
                            .keys
                    )
                    allTextModifications[flaw] = textModifications
                }
            }
            if (allFlawPositions == null) {
                allFlawPositions = flawPositions
            } else {
                allFlawPositions.removeIf { !flawPositions.contains(it) }
            }
        }
        return allTextModifications
            .filter { !it.value.modifiedPoints.isEmpty() && !it.value.modifiedPoints.all { entry -> entry.value is FlawTextModifications.NoModification } }
            .map {
                it.key to it.value.withMatching { pos ->
                    allFlawPositions?.contains(pos) ?: false
                }
            }.toMap()
    }

    fun estimateFragmentation(recipeView: RecipeView): Double {
        val recipe = Recipes.recipes()[recipeView.recipeIdentifier] ?: return 100.0
        if (recipe.steps.isEmpty()) return 0.0

        var fragmentation = 0.0

        recipe.steps.forEachIndexed { idx, step ->
            val base = resolveTranslatablesForMutation(buildBaseStep(step))
            val modifications = compileTextModifications(base, idx, recipeView.flaws)
            if (modifications.isEmpty()) {
                return@forEachIndexed
            }
            fragmentation += modifications.values.sumOf { it.intensity() }
        }

        return fragmentation / recipe.steps.size * 100.0
    }

    fun clearRedundantFlaws(view: RecipeView, thresholdPercent: Double = 15.0): RecipeView {
        val applicableFlaws = mutableSetOf<Flaw>()
        val recipe = Recipes.recipes()[view.recipeIdentifier] ?: return view
        recipe.steps.forEachIndexed { index, step ->
            compileTextModifications(resolveTranslatablesForMutation(buildBaseStep(step)), index, view.flaws)
                .keys.forEach { applicableFlaws.add(it) }
        }

        val bundles = mutableListOf<FlawBundle>()
        for (bundle in view.flaws) {
            bundles.add(
                FlawBundle(
                    bundle.flaws
                        .filter { applicableFlaws.contains(it) })
            )
        }
        val pct = estimateFragmentation(view)
        return if (pct < thresholdPercent) {
            RecipeView(view.recipeIdentifier, emptyList())
        } else {
            RecipeView(view.recipeIdentifier, bundles)
        }
    }

    private fun compileIngredients(ingredients: Map<Ingredient, Int>): Component {
        return ingredients.entries.stream()
            .map { entry ->
                Component.text(entry.value).color(NamedTextColor.GOLD).appendSpace().append(
                    entry.key.displayName // A Component, not supported
                ).colorIfAbsent(NamedTextColor.GRAY)
            }.collect(Component.toComponent(Component.text(", ")))
    }

    private fun flawApplies(stepIndex: Int, flaw: Flaw): Boolean {
        return when (flaw.config.extent) {
            is FlawExtent.Everywhere -> true
            is FlawExtent.WholeStep -> stepIndex == flaw.config.extent.stepIndex
            is FlawExtent.PartialStep -> stepIndex == flaw.config.extent.stepIndex
            else -> false
        }
    }

    private fun resolveTranslatablesForMutation(node: Component): Component {
        val mappedChildren = node.children().map { resolveTranslatablesForMutation(it) }
        val withChildren = node.children(mappedChildren)

        return when (withChildren) {
            is TranslatableComponent -> {
                val rendered = TranslationUtil.render(withChildren)
                if (rendered !is TranslatableComponent) {
                    resolveTranslatablesForMutation(rendered).style(withChildren.style())
                } else {
                    Component.text(humanizeTranslationKey(withChildren.key()))
                        .style(withChildren.style())
                }
            }

            else -> withChildren
        }
    }

    private fun humanizeTranslationKey(key: String): String {
        // e.g. "block.minecraft.short_grass" -> "Short Grass"
        val part = key.substringAfterLast('.')
        if (part.isEmpty()) return key
        return part.split('_').joinToString(" ") { w ->
            w.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
        }
    }

}