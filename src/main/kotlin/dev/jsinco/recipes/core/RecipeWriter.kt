package dev.jsinco.recipes.core

import dev.jsinco.recipes.Recipes
import dev.jsinco.recipes.core.flaws.Flaw
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


    private fun renderStep(step: Step, stepIndex: Int, flaws: List<Flaw>): Component {
        var output: Component = when (step) {
            is DistillStep -> Component.translatable(
                "recipes.display.recipe.step.distill", Argument.tagResolver(
                    Formatter.number(
                        "distill_runs", step.count
                    )
                )
            )

            is AgeStep -> Component.translatable(
                "recipes.display.recipe.step.age", Argument.tagResolver(
                    Formatter.number(
                        "aging_years",
                        step.agingTicks / DEFAULT_AGING_YEAR
                    ),
                    Placeholder.component(
                        "barrel_type",
                        Component.translatable("recipes.barrel.type." + step.barrelType.name.lowercase(Locale.ROOT))
                    )
                )
            )

            is MixStep -> Component.translatable(
                "recipes.display.recipe.step.mix", Argument.tagResolver(
                    Placeholder.component(
                        "ingredients", compileIngredients(step.ingredients)
                    ),
                    Formatter.number(
                        "mixing_time",
                        step.mixingTicks / DEFAULT_COOKING_MINUTE
                    )
                )
            )

            is CookStep -> Component.translatable(
                "recipes.display.recipe.step.cook", Argument.tagResolver(
                    Placeholder.component(
                        "ingredients", compileIngredients(step.ingredients)
                    ),
                    Formatter.number(
                        "cooking_time",
                        step.cookingTicks / DEFAULT_COOKING_MINUTE
                    ),
                    Placeholder.component(
                        "cauldron_type", Component.translatable(
                            "recipes.cauldron.type." + step.cauldronType.name.lowercase(Locale.ROOT)
                        )
                    )
                )
            )

            else -> {
                Component.text { "Unknown component" }
            }
        }
        output = TranslationUtil.render(output)
        val flawsMatches =
            flaws.filter { flawApplies(stepIndex, it) }
        for (flaw in flawsMatches) {
            output = flaw.type.applyTo(output, flaw.extent)
        }
        return output
    }

    private fun compileIngredients(ingredients: Map<Ingredient, Int>): Component {
        return ingredients.entries.stream()
            .map { entry ->
                Component.text(entry.value).color(NamedTextColor.GOLD).appendSpace().append(
                    entry.key.displayName
                ).colorIfAbsent(NamedTextColor.GRAY)
            }.collect(Component.toComponent(Component.text(", ")))
    }

    private fun flawApplies(stepIndex: Int, flaw: Flaw): Boolean {
        return when (flaw.extent) {
            is FlawExtent.Everywhere -> true
            is FlawExtent.WholeStep -> stepIndex == flaw.extent.stepIndex
            is FlawExtent.PartialStep -> stepIndex == flaw.extent.stepIndex
            else -> false
        }
    }

}