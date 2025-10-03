package dev.jsinco.recipes.core.flaws

import net.kyori.adventure.text.Component

interface FlawType {

    fun applyTo(component: Component, config: FlawConfig): Component
}