package dev.jsinco.recipes.core.flaws.type

import dev.jsinco.recipes.core.flaws.FlawConfig
import dev.jsinco.recipes.core.flaws.FlawTextModificationWriter
import dev.jsinco.recipes.core.flaws.FlawTextModifications
import net.kyori.adventure.text.Component

data class ReplacementFlawType(val replacement: String) : FlawType {

    override fun postProcess(text: String, pos: Int, config: FlawConfig): Component {
        return Component.text(text)
    }

    override fun findFlawModifications(
        component: Component,
        session: FlawType.ModificationFindSession
    ): FlawTextModifications {
        return FlawTextModificationWriter.randomPositionReplacement(component, session, 1.0) {
            replacement.repeat(it.length)
        }
    }

    override fun estimatedObscurationIntensity(intensity: Double) = intensity

}