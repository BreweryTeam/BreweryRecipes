package dev.jsinco.recipes.recipe.process.steps

import dev.jsinco.recipes.recipe.RecipeViewLoreWriter.cookingMinuteTicks
import dev.jsinco.recipes.recipe.process.Ingredient
import dev.jsinco.recipes.recipe.process.IngredientStep
import dev.jsinco.recipes.recipe.process.StepType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.translation.Argument

class MixStep(val mixingTicks: Long, val cauldronType: CauldronType, val ingredients: Map<Ingredient, Int>) :
    IngredientStep {

    override fun getType(): StepType = StepType.MIX
    override fun display(): Component = Component.translatable(
        "recipes.display.recipe.step.mix",
        Argument.tagResolver(Formatter.number("mixing_time", mixingTicks / cookingMinuteTicks))
    )

    override fun ingredients(): Map<Ingredient, Int> = ingredients

    enum class CauldronType(val colorHex: String) {
        WATER("#7ec0d4"),
        LAVA("#e07848"),
        SNOW("#c0dce8");

        companion object {
            @JvmStatic
            fun fromString(type: String?): CauldronType {
                if (type == null) return WATER
                return when (type.trim().lowercase()) {
                    "snow" -> SNOW
                    "lava" -> LAVA
                    else -> WATER
                }
            }
        }
    }
}
