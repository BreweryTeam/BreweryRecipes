package dev.jsinco.recipes.core.flaws

import kotlin.random.Random

interface FlawExtent {

    fun obscurationLevel(recipeStepAmount: Int): Double

    fun appliesTo(stepIndex: Int, pos: Int): Boolean

    object Everywhere : FlawExtent {
        override fun obscurationLevel(recipeStepAmount: Int) = 1.0

        override fun appliesTo(stepIndex: Int, pos: Int) = true
    }

    data class WholeStep(val stepIndex: Int) : FlawExtent {
        override fun obscurationLevel(recipeStepAmount: Int) = 1.0 / recipeStepAmount

        override fun appliesTo(stepIndex: Int, pos: Int) = stepIndex == this.stepIndex
    }

    data class StepRange(val stepIndex: Int, val start: Int, val stop: Int) : FlawExtent {
        override fun obscurationLevel(recipeStepAmount: Int) = 0.3 / recipeStepAmount

        override fun appliesTo(stepIndex: Int, pos: Int): Boolean {
            return start <= pos && pos < stop && this.stepIndex == stepIndex
        }
    }

    data class AfterPoint(val stepIndex: Int, val start: Int) : FlawExtent {
        override fun obscurationLevel(recipeStepAmount: Int) =
            (recipeStepAmount - stepIndex).toDouble() / recipeStepAmount

        override fun appliesTo(stepIndex: Int, pos: Int): Boolean {
            return this.stepIndex < stepIndex || (stepIndex == this.stepIndex && start <= pos)
        }
    }

    companion object {
        fun compileWholeStep(steps: Int): WholeStep {
            return WholeStep(Random.nextInt(steps))
        }

        fun compileStepRange(steps: Int): StepRange {
            val from = Random.nextInt(10)
            return StepRange(Random.nextInt(steps), from, from + Random.nextInt(10) + 10)
        }

        fun compileAfterPoint(steps: Int): AfterPoint {
            return AfterPoint(Random.nextInt(steps), Random.nextInt(10))
        }
    }
}