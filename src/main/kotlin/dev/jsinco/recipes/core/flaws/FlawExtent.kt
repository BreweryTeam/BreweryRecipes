package dev.jsinco.recipes.core.flaws

interface FlawExtent {

    fun obscurationLevel(recipeStepAmount: Int): Double

    fun appliesTo(pos: Int): Boolean

    class Everywhere : FlawExtent {
        override fun obscurationLevel(recipeStepAmount: Int) = 1.0

        override fun appliesTo(pos: Int) = true
    }

    data class WholeStep(val stepIndex: Int) : FlawExtent {
        override fun obscurationLevel(recipeStepAmount: Int) = 1.0 / recipeStepAmount

        override fun appliesTo(pos: Int) = true
    }

    data class PartialStep(val stepIndex: Int, val start: Int, val stop: Int) : FlawExtent {
        override fun obscurationLevel(recipeStepAmount: Int) = 0.5 / recipeStepAmount

        override fun appliesTo(pos: Int): Boolean {
            return start <= pos && pos < stop
        }
    }
}