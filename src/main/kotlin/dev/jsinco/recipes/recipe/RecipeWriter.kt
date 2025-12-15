package dev.jsinco.recipes.recipe

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.recipe.flaws.Flaw
import dev.jsinco.recipes.recipe.flaws.FlawExtent
import dev.jsinco.recipes.recipe.flaws.FlawTextModificationWriter
import dev.jsinco.recipes.recipe.flaws.FlawTextModifications
import dev.jsinco.recipes.recipe.flaws.type.FlawType
import dev.jsinco.recipes.recipe.process.Ingredient
import dev.jsinco.recipes.recipe.process.Step
import dev.jsinco.recipes.recipe.process.steps.AgeStep
import dev.jsinco.recipes.recipe.process.steps.CookStep
import dev.jsinco.recipes.recipe.process.steps.DistillStep
import dev.jsinco.recipes.recipe.process.steps.MixStep
import dev.jsinco.recipes.gui.integration.GuiIntegration
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
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*

object RecipeWriter {

    var cookingMinuteTicks = 20L * 60L
    var agingYearTicks = 20L * 60L * 20L

    fun writeItem(recipeView: RecipeView, guiIntegration: GuiIntegration): ItemStack? {
        cookingMinuteTicks = guiIntegration.cookingMinuteTicks()
        agingYearTicks = guiIntegration.agingYearTicks()
        val recipe = Recipes.recipes()[recipeView.recipeIdentifier] ?: return null
        val item = if (Recipes.guiConfig.recipes.enabled) {
            Recipes.guiConfig.recipes.item.generateItem()
        } else {
            guiIntegration.createItem(recipeView)
        } ?: ItemStack(Material.BARRIER)
        item.setData(
            DataComponentTypes.LORE, ItemLore.lore(
                recipe.steps
                    .asSequence()
                    .mapIndexed { index, value ->
                        renderStep(
                            value,
                            index,
                            recipeView.flaws,
                            recipeView.invertedReveals
                        )
                    }
                    .map { component ->
                        component.colorIfAbsent(NamedTextColor.GRAY)
                            .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    }.map(TranslationUtil::render)
                    .toList()
            )
        )
        val displayName = guiIntegration.brewDisplayName(recipeView.recipeIdentifier)
            ?.let { recipeView.translation(it) } ?: return null
        item?.setData(
            DataComponentTypes.CUSTOM_NAME,
            TranslationUtil.render(displayName)
                .colorIfAbsent(NamedTextColor.WHITE)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
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
                    Formatter.number("aging_years", step.agingTicks / agingYearTicks),
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
                    Formatter.number("mixing_time", step.mixingTicks / cookingMinuteTicks)
                )
            )

            is CookStep -> Component.translatable(
                "recipes.display.recipe.step.cook",
                Argument.tagResolver(
                    Placeholder.component("ingredients", compileIngredients(step.ingredients)),
                    Formatter.number("cooking_time", step.cookingTicks / cookingMinuteTicks),
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

    private fun renderStep(step: Step, stepIndex: Int, flaws: List<Flaw>, reveals: List<Set<Int>>): Component {
        val rawBase = buildBaseStep(step)
        val base = resolveTranslatablesForMutation(rawBase)
        val textModifications = compileTextModifications(base, stepIndex, flaws)
            .map { it.key to it.value.withMatching { idx -> reveals.isEmpty() || reveals[stepIndex].contains(idx) } }
            .toMap()
        var output = base
        var offsets = mapOf<Int, Int>()
        for (flaw in flaws) {
            val textModification = textModifications[flaw] ?: continue
            output =
                FlawTextModificationWriter.process(output, textModification, flaw, offsets)
            offsets = textModification.offsets(offsets)
        }
        return output
    }

    private fun compileTextModifications(
        step: Component,
        stepIndex: Int,
        flaws: List<Flaw>
    ): Map<Flaw, FlawTextModifications> {
        val allTextModifications = mutableMapOf<Flaw, FlawTextModifications>()
        if (flaws.isEmpty()) {
            return allTextModifications
        }
        val flawPositions = mutableListOf<Int>()
        for (flaw in flaws) {
            if (flawApplies(stepIndex, flaw)) {
                val session = FlawType.ModificationFindSession(stepIndex, flaw.config) {
                    !flawPositions.contains(it)
                }
                val textModifications = flaw.type.findFlawModifications(step, session)
                flawPositions.addAll(
                    textModifications.modifiedPoints
                        .keys
                )
                allTextModifications[flaw] = textModifications
            }
        }
        return allTextModifications
            .filter { !it.value.modifiedPoints.isEmpty() && !it.value.modifiedPoints.all { entry -> entry.value is FlawTextModifications.NoModification } }
    }

    fun estimateFragmentation(recipeView: RecipeView): Double {
        val recipe = Recipes.recipes()[recipeView.recipeIdentifier] ?: return 100.0
        if (recipe.steps.isEmpty()) return 0.0

        var fragmentation = 0.0

        recipe.steps.forEachIndexed { idx, step ->
            val base = resolveTranslatablesForMutation(buildBaseStep(step))
            val approxBaseLength = PlainTextComponentSerializer.plainText().serialize(base).length
            val modifications = compileTextModifications(base, idx, recipeView.flaws)
                .map {
                    it.key to it.value.withMatching { pos ->
                        recipeView.invertedReveals.isEmpty() || recipeView.invertedReveals[idx].contains(pos)
                    }
                }.toMap()
            if (modifications.isEmpty()) {
                return@forEachIndexed
            }
            fragmentation += modifications.values.sumOf { it.intensity(approxBaseLength) }
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

        val newFlaws = view.flaws.filter { applicableFlaws.contains(it) }
        val pct = estimateFragmentation(view)
        return if (pct < thresholdPercent) {
            RecipeView(view.recipeIdentifier, emptyList(), emptyList())
        } else {
            RecipeView(view.recipeIdentifier, newFlaws, view.invertedReveals)
        }
    }

    fun mergeFlaws(base: RecipeView, toSubtract: RecipeView): RecipeView {
        val recipe = Recipes.recipes()[base.recipeIdentifier] ?: return base
        val flawPositions = mutableListOf<MutableSet<Int>>()
        for (i in 0..<recipe.steps.size) {
            val step = recipe.steps[i]
            val positions = mutableSetOf<Int>()
            for (flaw in toSubtract.flaws) {
                if (flawApplies(i, flaw)) {
                    val session = FlawType.ModificationFindSession(i, flaw.config) {
                        !positions.contains(it)
                    }
                    val textModifications = flaw.type.findFlawModifications(buildBaseStep(step), session)
                    positions.addAll(
                        textModifications.modifiedPoints
                            .keys
                    )
                }
            }
            flawPositions.add(positions)
        }
        val invertedReveals = if (base.invertedReveals.isEmpty()) {
            flawPositions
        } else {
            base.invertedReveals
                .mapIndexed { idx, stepReveals ->
                    stepReveals.filter { andMask(it, flawPositions[idx]) }
                        .toSet()
                }
        }
        return RecipeView(
            base.recipeIdentifier, base.flaws, invertedReveals
        )
    }

    private fun andMask(i: Int, ints: Set<Int>, radius: Int = 1): Boolean {
        for (idx in (i - radius)..<(i + radius)) {
            if (!ints.contains(idx)) {
                return false
            }
        }
        return true
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
            is FlawExtent.StepRange -> stepIndex == flaw.config.extent.stepIndex
            is FlawExtent.AfterPoint -> stepIndex >= flaw.config.extent.stepIndex
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