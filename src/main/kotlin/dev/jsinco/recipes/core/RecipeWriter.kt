package dev.jsinco.recipes.core

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.core.flaws.Flaw
import dev.jsinco.recipes.core.flaws.FlawExtent
import dev.jsinco.recipes.core.flaws.number.NumberFlawType
import dev.jsinco.recipes.core.flaws.text.TextFlawType
import dev.jsinco.recipes.core.process.Ingredient
import dev.jsinco.recipes.core.process.Step
import dev.jsinco.recipes.core.process.steps.AgeStep
import dev.jsinco.recipes.core.process.steps.CookStep
import dev.jsinco.recipes.core.process.steps.DistillStep
import dev.jsinco.recipes.core.process.steps.MixStep
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
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
    // TODO: Make this convert a BreweryRecipe to readable instructions based on language files and each step's flaws


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


    private fun renderStep(step: Step, stepIndex: Int, flaws: List<Flaw>): Component {
        val stepFlaws = flaws.filter { flawApplies(stepIndex, it) }
        return when (step) {
            is DistillStep -> Component.translatable(
                "recipes.display.recipe.step.distill", Argument.tagResolver(
                    Formatter.number("distill_runs", step.count)
                )
            )

            is AgeStep -> Component.translatable(
                "recipes.display.recipe.step.age", Argument.tagResolver(
                    Formatter.number("aging_years", step.agingTicks),
                    Placeholder.component(
                        "barrel_type",
                        Component.translatable("recipes.barrel.type." + step.barrelType.name.lowercase(Locale.ROOT))
                    )
                )
            )

            is MixStep -> Component.translatable(
                "recipes.display.recipe.step.mix", Argument.tagResolver(
                    Placeholder.component(
                        "ingredients", compileIngredients(step.ingredients, flaws)
                    ),
                    Formatter.number("mixing_time", step.mixingTicks)
                )
            )

            is CookStep -> Component.translatable(
                "recipes.display.recipe.step.cook", Argument.tagResolver(
                    Placeholder.component(
                        "ingredients", compileIngredients(step.ingredients, flaws)
                    ),
                    Formatter.number("cooking_time", step.ticks),
                    Placeholder.component(
                        "cauldron_type",
                        Component.translatable(
                            "recipes.cauldron.type." + step.cauldronType.name.lowercase(Locale.ROOT)
                        )
                    )
                )
            )

            else -> {
                Component.text { "Unknown component" }
            }
        }
    }

    private fun compileIngredients(ingredients: Map<Ingredient, Int>, flaws: List<Flaw>): Component {
        val complete = ingredients.entries.stream()
            .map { entry ->
                val flawMatch = flaws.asSequence()
                    .filter { it.extent is FlawExtent.PartialStep && it.extent.part == "ingredient" }
                    .map { it.type }
                    .firstOrNull()
                val amount =
                    if (flawMatch is NumberFlawType) flawMatch.applyTo(entry.value.toLong()).toInt() else entry.value
                val item = Component.text(amount).color(NamedTextColor.GOLD).appendSpace().append(
                    entry.key.displayName
                ).colorIfAbsent(NamedTextColor.GRAY)
                if (flawMatch is TextFlawType) flawMatch.applyTo(item) else item
            }.collect(Component.toComponent(Component.text(", ")))
        val flawMatch = flaws.asSequence()
            .filter { it.extent is FlawExtent.PartialStep && it.extent.part == "ingredient" }
            .map { it.type }
            .filterIsInstance<TextFlawType>()
            .firstOrNull()
        flawMatch?.let { return flawMatch.applyTo(complete) }
        return complete
    }

    private fun flawApplies(stepIndex: Int, flaw: Flaw): Boolean {
        return when (flaw.extent) {
            is FlawExtent.Everywhere -> true
            is FlawExtent.WholeStep -> stepIndex == flaw.extent.stepIndex
            is FlawExtent.PartialStep -> stepIndex == flaw.extent.stepIndex
            is FlawExtent.ExactIngredient -> stepIndex == flaw.extent.stepIndex
            else -> false
        }
    }
}