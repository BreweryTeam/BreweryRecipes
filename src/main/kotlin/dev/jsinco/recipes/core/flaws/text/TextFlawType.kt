package dev.jsinco.recipes.core.flaws.text

import dev.jsinco.recipes.core.flaws.FlawType
import net.kyori.adventure.text.Component

interface TextFlawType : FlawType {

    fun applyTo(component: Component): Component
}