package dev.jsinco.recipes.core.process.steps

import dev.jsinco.recipes.core.decoration.Flaw
import dev.jsinco.recipes.core.process.Step
import dev.jsinco.recipes.core.process.StepType

class CookStep(private val ticks: Long, private val cauldronType: CauldronType, private val ingredients: Map<String, Int>, private val flaws: List<Flaw>) : Step {

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