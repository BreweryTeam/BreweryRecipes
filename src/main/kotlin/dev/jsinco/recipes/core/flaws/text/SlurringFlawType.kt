package dev.jsinco.recipes.core.flaws.text

import net.kyori.adventure.text.Component

class SlurringFlawType(val intensity: Double, val seeds: List<Int>) : TextFlawType {

    override fun intensity() = intensity
    override fun applyTo(
        component: Component
    ): Component {
        TODO("Not yet implemented")
    }

    override fun seeds() = seeds
}