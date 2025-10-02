package dev.jsinco.recipes.core.flaws

import net.kyori.adventure.text.Component

interface FlawType {

    fun intensity(): Double

    fun seeds(): List<Int>

    fun applyTo(component: Component, extent: FlawExtent): Component
}