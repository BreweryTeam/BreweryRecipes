package dev.jsinco.recipes.recipe.process.steps

import dev.jsinco.recipes.recipe.process.Step
import dev.jsinco.recipes.recipe.process.StepType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.translation.Argument

class DistillStep(val count: Long) : Step {

    override fun getType(): StepType = StepType.DISTILL
    override fun display(): Component = Component.translatable(
        "recipes.display.recipe.step.distill.v2",
        Argument.tagResolver(Formatter.number("distill_runs", count))
    )


}