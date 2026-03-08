package dev.jsinco.recipes.recipe.process.steps

import dev.jsinco.recipes.recipe.RecipeWriter.agingYearTicks
import dev.jsinco.recipes.recipe.process.Step
import dev.jsinco.recipes.recipe.process.StepType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.translation.Argument
import java.util.*

class AgeStep(val agingTicks: Long, val barrelType: BarrelType) : Step {

    override fun getType(): StepType = StepType.AGE
    override fun display(): Component = Component.translatable(
        "recipes.display.recipe.step.age",
        Argument.tagResolver(
            Formatter.number("aging_years", agingTicks / agingYearTicks),
            Placeholder.component(
                "barrel_type",
                Component.translatable(
                    "recipes.barrel.type." + barrelType.name.lowercase(
                        Locale.ROOT
                    )
                )
            )
        )
    )

    enum class BarrelType {
        ANY, BIRCH, OAK, JUNGLE, SPRUCE, ACACIA, DARK_OAK, CRIMSON, WARPED, MANGROVE, CHERRY, BAMBOO, COPPER, PALE_OAK;

        companion object {
            @JvmStatic
            fun fromString(type: String?): BarrelType {
                if (type == null) return ANY
                return entries.firstOrNull { it.name.equals(type.trim(), ignoreCase = true) } ?: ANY
            }
        }
    }


}