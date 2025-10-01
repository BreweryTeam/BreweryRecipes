package dev.jsinco.recipes.core.flaws.number

import dev.jsinco.recipes.core.flaws.FlawType

interface NumberFlawType : FlawType {

    fun apply(number: Long, intensity/*0-100*/: Double): Long

}