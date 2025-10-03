package dev.jsinco.recipes.core

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.core.flaws.Flaw
import dev.jsinco.recipes.core.flaws.FlawBundle
import dev.jsinco.recipes.core.flaws.FlawExtent
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
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
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
        val rawBase = buildBaseStep(step) // Ingredient names not yet resolved / still translatable components
        val base = resolveTranslatablesForMutation(rawBase) // Resolve ingredient names
        val baseChars = flattenToStyledChars(base)

        val bundleResults: List<Component> = flaws.map { bundle ->
            val applicableFlaws = bundle.flaws.filter { flawApplies(stepIndex, it) }
            if (applicableFlaws.isEmpty()) {
                base // This bundle contributes nothing, its result equals base
            } else {
                val variants = applicableFlaws.map { flaw ->
                    flaw.type.applyTo(base, flaw.config)
                }
                mergePreferFlawed(base, baseChars, variants)
            }
        }
        return mergePreferUnflawed(base, baseChars, bundleResults)
    }

    fun estimateFragmentation(recipeView: RecipeView): Double {
        val recipe = Recipes.recipes()[recipeView.recipeIdentifier] ?: return 100.0
        if (recipe.steps.isEmpty()) return 0.0

        var differing = 0L
        var total = 0L

        recipe.steps.forEachIndexed { idx, step ->
            val base = buildBaseStep(step)
            val final = renderStep(step, idx, recipeView.flaws)

            val baseChars = flattenToStyledChars(base)
            val finalChars = flattenToStyledChars(final)

            val maxLen = maxOf(baseChars.size, finalChars.size)
            for (i in 0 until maxLen) {
                val b = baseChars.getOrNull(i)
                val f = finalChars.getOrNull(i)
                if (b != null || f != null) {
                    total += 1
                    val isSame = (b != null && f != null && b.ch == f.ch && b.style == f.style)
                    if (!isSame) differing += 1
                }
            }
        }

        if (total == 0L) return 0.0
        return (differing.toDouble() / total.toDouble()) * 100.0
    }

    fun normalizeFlawsIfLowFragmentation(view: RecipeView, thresholdPercent: Double = 15.0): RecipeView {
        val pct = estimateFragmentation(view)
        return if (pct < thresholdPercent) {
            RecipeView(view.recipeIdentifier, emptyList())
        } else {
            view
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
        var withChildren = node.children(mappedChildren)

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

    private data class StyledChar(val ch: Char, val style: Style)
    private fun flattenToStyledChars(component: Component): List<StyledChar> {
        val out = ArrayList<StyledChar>(64)

        fun visit(node: Component, inherited: Style, depth: Int) {
            val current = inherited.merge(node.style(), Style.Merge.Strategy.IF_ABSENT_ON_TARGET)

            when (node) {
                is TextComponent -> {
                    val s = node.content()
                    for (c in s) out += StyledChar(c, current)
                    node.children().forEach { visit(it, current, depth + 1) }
                }

                is TranslatableComponent -> {
                    val rendered = TranslationUtil.render(node)
                    if (rendered !== node && rendered !is TranslatableComponent) {
                        visit(rendered, current, depth + 1)
                        return
                    }

                    val fallback = humanizeTranslationKey(node.key())
                    for (c in fallback) out += StyledChar(c, current)

                    node.children().forEach { visit(it, current, depth + 1) }
                }

                else -> {
                    node.children().forEach { visit(it, current, depth + 1) }
                }
            }
        }

        visit(component, Style.empty(), 0)
        return out
    }
    private fun buildComponentFromStyledChars(chars: List<StyledChar>): Component {
        if (chars.isEmpty()) return Component.empty()
        val builder = Component.text()
        var i = 0
        while (i < chars.size) {
            val startStyle = chars[i].style
            val sb = StringBuilder()
            sb.append(chars[i].ch)
            var j = i + 1
            while (j < chars.size && chars[j].style == startStyle) {
                sb.append(chars[j].ch)
                j++
            }
            builder.append(Component.text(sb.toString()).style(startStyle))
            i = j
        }
        return builder.build()
    }

    private fun mergePreferFlawed(base: Component, baseChars: List<StyledChar>, variants: List<Component>): Component {
        if (variants.isEmpty()) return base

        val variantChars: List<List<StyledChar>> = variants.map { flattenToStyledChars(it) }
        val maxLen = listOf(baseChars.size, *variantChars.map { it.size }.toTypedArray()).maxOrNull() ?: 0
        val result = ArrayList<StyledChar>(maxLen)

        for (idx in 0 until maxLen) {
            val baseAt = baseChars.getOrNull(idx)
            // If any variant differs (char or style) from base at idx, pick the first differing variant char+style
            var chosen: StyledChar? = null
            for (v in variantChars) {
                val vAt = v.getOrNull(idx)
                if (vAt != null && baseAt != null) {
                    if (vAt.ch != baseAt.ch || vAt.style != baseAt.style) {
                        chosen = vAt
                        break
                    }
                } else if (vAt != null && baseAt == null) {
                    // Variant extended length -> consider this a difference
                    chosen = vAt
                    break
                }
            }
            result += when {
                chosen != null -> chosen
                baseAt != null -> baseAt
                else -> StyledChar(' ', Style.empty()) // fallback padding
            }
        }
        return buildComponentFromStyledChars(result)
    }
    private fun mergePreferUnflawed(base: Component, baseChars: List<StyledChar>, bundleResults: List<Component>): Component {
        if (bundleResults.isEmpty()) return base

        val bundleChars: List<List<StyledChar>> = bundleResults.map { flattenToStyledChars(it) }
        val maxLen = listOf(baseChars.size, *bundleChars.map { it.size }.toTypedArray()).maxOrNull() ?: 0
        val result = ArrayList<StyledChar>(maxLen)

        for (idx in 0 until maxLen) {
            val baseAt = baseChars.getOrNull(idx)

            // If any bundle shows base (same char AND style), we keep base there (i.e., cancel flaws)
            val anyShowsBase = bundleChars.any { b ->
                val bAt = b.getOrNull(idx)
                baseAt != null && bAt != null && bAt.ch == baseAt.ch && bAt.style == baseAt.style
            }

            if (anyShowsBase && baseAt != null) {
                result += baseAt
                continue
            }

            // Otherwise, take the first bundle's char at that position if present; else base or padding
            val firstNonNull = bundleChars.firstNotNullOfOrNull { it.getOrNull(idx) }
            result += when {
                firstNonNull != null -> firstNonNull
                baseAt != null -> baseAt
                else -> StyledChar(' ', Style.empty())
            }
        }
        return buildComponentFromStyledChars(result)
    }

}