package dev.jsinco.recipes.core.flaws.text

import dev.jsinco.recipes.core.flaws.FlawExtent
import dev.jsinco.recipes.core.flaws.FlawType
import net.kyori.adventure.text.Component

class SlurringFlawType(val intensity: Double, val seeds: List<Int>) : FlawType {

    override fun intensity() = intensity
    override fun applyTo(
        component: Component, extent: FlawExtent
    ): Component {
        TODO("Not yet implemented")
    }

    override fun seeds() = seeds
}