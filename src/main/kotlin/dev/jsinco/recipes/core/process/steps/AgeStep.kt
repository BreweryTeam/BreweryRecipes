package dev.jsinco.recipes.core.process.steps

import dev.jsinco.recipes.core.process.Step
import dev.jsinco.recipes.core.process.StepType

class AgeStep(val agingTicks: Long, val barrelType: BarrelType) : Step {

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