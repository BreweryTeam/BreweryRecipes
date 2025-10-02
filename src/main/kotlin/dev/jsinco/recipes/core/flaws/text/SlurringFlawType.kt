package dev.jsinco.recipes.core.flaws.text

import net.kyori.adventure.text.Component

class SlurringFlawType(val intensity: Double) : TextFlawType {

    override fun applyTo(component: Component): Component {
        TODO("Need to expose the tbp text transformer in API")
    }

    override fun intensity() = intensity
}