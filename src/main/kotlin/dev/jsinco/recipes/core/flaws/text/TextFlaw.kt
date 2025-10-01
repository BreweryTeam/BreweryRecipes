package dev.jsinco.recipes.core.flaws.text

import dev.jsinco.recipes.core.flaws.Flaw

interface TextFlaw : Flaw {

    fun apply(text: String, intensity/*0-100*/: Double): String

}