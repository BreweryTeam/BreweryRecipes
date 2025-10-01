package dev.jsinco.recipes.core

import com.google.common.collect.ImmutableList
import dev.jsinco.recipes.core.flaws.Flaw
import dev.jsinco.recipes.core.process.Step
import dev.jsinco.recipes.core.process.steps.AgeStep
import dev.jsinco.recipes.core.process.steps.CookStep
import dev.jsinco.recipes.core.process.steps.DistillStep
import dev.jsinco.recipes.core.process.steps.MixStep

class BreweryRecipe(private val identifier: String, private val steps: List<Step>) {

    private var name: String? = null

    // TODO: Make the BX and TBP integrations use this builder to construct all of their registered recipes, so we can make recipes for them
    class Builder(private val identifier: String) {
        private val stepsBuilder = ImmutableList.Builder<Step>()

        fun mix(ticks: Long, cauldronType: String, ingredients: Map<String, Int>, flaws: List<Flaw>) = apply {
            stepsBuilder.add(MixStep(ticks, MixStep.CauldronType.fromString(cauldronType), ingredients, flaws))
        }

        fun cook(ticks: Long, cauldronType: String, ingredients: Map<String, Int>, flaws: List<Flaw>) = apply {
            stepsBuilder.add(CookStep(ticks, CookStep.CauldronType.fromString(cauldronType), ingredients, flaws))
        }

        fun distill(count: Long, flaws: List<Flaw>) = apply { stepsBuilder.add(DistillStep(count, flaws)) }

        fun age(ticks: Long, barrelType: String, flaws: List<Flaw>) = apply {
            stepsBuilder.add(AgeStep(ticks, AgeStep.BarrelType.fromString(barrelType), flaws))
        }

        fun build() = BreweryRecipe(identifier, stepsBuilder.build())
    }

}