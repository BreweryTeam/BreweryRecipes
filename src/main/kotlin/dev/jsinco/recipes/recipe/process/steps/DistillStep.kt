package dev.jsinco.recipes.recipe.process.steps

import dev.jsinco.recipes.recipe.process.Step
import dev.jsinco.recipes.recipe.process.StepType

class DistillStep(val count: Long) : Step {

    override fun getType(): StepType = StepType.DISTILL


}