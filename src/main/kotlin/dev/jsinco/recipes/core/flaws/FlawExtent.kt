package dev.jsinco.recipes.core.flaws

interface FlawExtent {

    fun obscurationLevel(recipeStepAmount: Int): Double

    fun appliesTo(stepIndex: Int, pos: Int): Boolean

    class Everywhere : FlawExtent {
        override fun obscurationLevel(recipeStepAmount: Int) = 1.0

        override fun appliesTo(stepIndex: Int, pos: Int) = true
    }

    data class WholeStep(val stepIndex: Int) : FlawExtent {
        override fun obscurationLevel(recipeStepAmount: Int) = 1.0 / recipeStepAmount

        override fun appliesTo(stepIndex: Int, pos: Int) = stepIndex == this.stepIndex
    }

    data class StepRange(val stepIndex: Int, val start: Int, val stop: Int) : FlawExtent {
        override fun obscurationLevel(recipeStepAmount: Int) = 0.5 / recipeStepAmount

        override fun appliesTo(stepIndex: Int, pos: Int): Boolean {
            return start <= pos && pos < stop && this.stepIndex == stepIndex
        }
    }

    data class AfterPoint(val stepIndex: Int, val start: Int) : FlawExtent {
        override fun obscurationLevel(recipeStepAmount: Int) = (stepIndex - recipeStepAmount) / recipeStepAmount - 0.5

        override fun appliesTo(stepIndex: Int, pos: Int): Boolean {
            return this.stepIndex < stepIndex || (stepIndex == this.stepIndex && start <= pos)
        }
    }
}