package dev.jsinco.recipes.core.process.steps

import dev.jsinco.recipes.core.decoration.Flaw
import dev.jsinco.recipes.core.process.Step
import dev.jsinco.recipes.core.process.StepType

class AgeStep(private val ticks: Long, private val barrelType: BarrelType, private val flaws: List<Flaw>) : Step {

    override fun getType(): StepType = StepType.AGE

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