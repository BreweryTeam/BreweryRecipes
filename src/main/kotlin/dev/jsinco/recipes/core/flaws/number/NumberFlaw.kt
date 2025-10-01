package dev.jsinco.recipes.core.flaws.number

import dev.jsinco.recipes.core.flaws.Flaw

interface NumberFlaw : Flaw {

    fun apply(number: Long, intensity/*0-100*/: Double): Long

}