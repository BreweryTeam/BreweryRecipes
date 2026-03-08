package dev.jsinco.recipes.recipe.process.steps

import dev.jsinco.recipes.recipe.RecipeWriter.cookingMinuteTicks
import dev.jsinco.recipes.recipe.process.Ingredient
import dev.jsinco.recipes.recipe.process.IngredientUtil
import dev.jsinco.recipes.recipe.process.Step
import dev.jsinco.recipes.recipe.process.StepType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.translation.Argument
import java.util.*

class CookStep(val cookingTicks: Long, val cauldronType: CauldronType, val ingredients: Map<Ingredient, Int>) : Step {

    override fun getType(): StepType = StepType.COOK
    override fun display(): Component = Component.translatable(
        "recipes.display.recipe.step.cook",
        Argument.tagResolver(
            Placeholder.component("ingredients", IngredientUtil.compileIngredients(ingredients)),
            Formatter.number("cooking_time", cookingTicks / cookingMinuteTicks),
            Placeholder.component(
                "cauldron_type",
                Component.translatable("recipes.cauldron.type." + cauldronType.name.lowercase(Locale.ROOT))
            )
        )
    )

    enum class CauldronType {
        WATER, LAVA, SNOW;

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