package dev.jsinco.recipes.core.process.steps

import dev.jsinco.recipes.core.process.Step
import dev.jsinco.recipes.core.process.StepType

class DistillStep(val count: Long) : Step {

    override fun getType(): StepType = StepType.DISTILL


}