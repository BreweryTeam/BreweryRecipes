package dev.jsinco.recipes.core.process.steps

import dev.jsinco.recipes.core.decoration.Flaw
import dev.jsinco.recipes.core.process.Step
import dev.jsinco.recipes.core.process.StepType

class DistillStep(private val count: Long, private val flaws: List<Flaw>) : Step {

    override fun getType(): StepType = StepType.DISTILL


}