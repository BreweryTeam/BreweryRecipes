package dev.jsinco.recipes.recipe.process.steps

import dev.jsinco.recipes.recipe.RecipeViewLoreWriter.agingYearTicks
import dev.jsinco.recipes.recipe.process.Step
import dev.jsinco.recipes.recipe.process.StepType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.translation.Argument

class AgeStep(val agingTicks: Long, val barrelType: BarrelType) : Step {

    override fun getType(): StepType = StepType.AGE
    override fun display(): Component = Component.translatable(
        "recipes.display.recipe.step.age",
        Argument.tagResolver(Formatter.number("aging_years", agingTicks / agingYearTicks))
    )

    enum class BarrelType(val colorHex: String) {
        ANY("#d4b87c"),
        BIRCH("#cdb768"),
        OAK("#c49840"),
        JUNGLE("#c88850"),
        SPRUCE("#9a7038"),
        ACACIA("#d06c2e"),
        DARK_OAK("#8a5828"),
        CRIMSON("#b84458"),
        WARPED("#48a890"),
        MANGROVE("#aa5048"),
        CHERRY("#e8a0a0"),
        BAMBOO("#c8b840"),
        COPPER("#c07840"),
        PALE_OAK("#c8c4c0");

        companion object {
            @JvmStatic
            fun fromString(type: String?): BarrelType {
                if (type == null) return ANY
                return entries.firstOrNull { it.name.equals(type.trim(), ignoreCase = true) } ?: ANY
            }
        }
    }
}
