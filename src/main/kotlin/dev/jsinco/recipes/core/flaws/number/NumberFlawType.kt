package dev.jsinco.recipes.core.flaws.number

import dev.jsinco.recipes.core.flaws.FlawType

interface NumberFlawType : FlawType {

    fun applyTo(number: Long): Long

}