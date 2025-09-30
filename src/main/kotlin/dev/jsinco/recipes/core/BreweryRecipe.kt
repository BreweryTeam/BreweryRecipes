package dev.jsinco.recipes.core

import dev.jsinco.recipes.core.decoration.Flaw
import dev.jsinco.recipes.core.process.Step
import dev.jsinco.recipes.core.process.steps.AgeStep
import dev.jsinco.recipes.core.process.steps.CookStep
import dev.jsinco.recipes.core.process.steps.DistillStep
import dev.jsinco.recipes.core.process.steps.MixStep

class BreweryRecipe(private val identifier: String) {

    private var name: String? = null
    private val steps: MutableList<Step> = mutableListOf()

    class Builder(private val identifier: String) {
        private val breweryRecipe = BreweryRecipe(identifier)

        fun name(name: String) = apply { breweryRecipe.name = name }

        fun mix(ticks: Long, cauldronType: String, ingredients: Map<String, Int>, flaws: List<Flaw>) = apply {
            breweryRecipe.steps.add(MixStep(ticks, MixStep.CauldronType.fromString(cauldronType), ingredients, flaws))
        }

        fun cook(ticks: Long, cauldronType: String, ingredients: Map<String, Int>, flaws: List<Flaw>) = apply {
            breweryRecipe.steps.add(CookStep(ticks, CookStep.CauldronType.fromString(cauldronType), ingredients, flaws))
        }

        fun distill(count: Long, flaws: List<Flaw>) = apply { breweryRecipe.steps.add(DistillStep(count, flaws)) }

        fun age(ticks: Long, barrelType: String, flaws: List<Flaw>) = apply {
            breweryRecipe.steps.add(AgeStep(ticks, AgeStep.BarrelType.fromString(barrelType), flaws))
        }

        fun build() = breweryRecipe
    }

}