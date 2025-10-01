package dev.jsinco.recipes.core.process.steps

import dev.jsinco.recipes.core.process.Ingredient
import dev.jsinco.recipes.core.process.Step
import dev.jsinco.recipes.core.process.StepType

class CookStep(val cookingTicks: Long, val cauldronType: CauldronType, val ingredients: Map<Ingredient, Int>) : Step {

    override fun getType(): StepType = StepType.COOK

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