package dev.jsinco.recipes.recipe.process.steps

import dev.jsinco.recipes.recipe.RecipeViewLoreWriter.cookingMinuteTicks
import dev.jsinco.recipes.recipe.process.Ingredient
import dev.jsinco.recipes.recipe.process.IngredientStep
import dev.jsinco.recipes.recipe.process.StepType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.translation.Argument

class CookStep(val cookingTicks: Long, val cauldronType: CauldronType, val ingredients: Map<Ingredient, Int>) :
    IngredientStep {

    override fun getType(): StepType = StepType.COOK
    override fun display(): Component = Component.translatable(
        "gui.recipes.lore.step.cook",
        Argument.tagResolver(Formatter.number("cooking_time", cookingTicks.toDouble() / cookingMinuteTicks))
    )
    override fun displayBrewNote(): Component = Component.translatable(
        "gui.recipes.lore.brew-note.step.cook",
        Argument.tagResolver(Formatter.number("cooking_time", cookingTicks.toDouble() / cookingMinuteTicks))
    )

    override fun ingredients(): Map<Ingredient, Int> = ingredients

    enum class CauldronType {
        WATER,
        LAVA,
        SNOW;

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
