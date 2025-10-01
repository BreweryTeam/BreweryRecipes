package dev.jsinco.recipes.core.flaws

interface FlawExtent {

    class Everywhere : FlawExtent

    data class WholeStep(val stepIndex: Int) : FlawExtent

    data class PartialStep(val stepIndex: Int, val part: String) : FlawExtent

    data class ExactIngredient(val stepIndex: Int, val ingredientKey: String) : FlawExtent
}