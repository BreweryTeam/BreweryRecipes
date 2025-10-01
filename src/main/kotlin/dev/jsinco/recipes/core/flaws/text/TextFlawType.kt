package dev.jsinco.recipes.core.flaws.text

import dev.jsinco.recipes.core.flaws.FlawType

interface TextFlawType : FlawType {

    fun apply(text: String, intensity/*0-100*/: Double): String

}